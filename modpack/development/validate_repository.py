"""Valida dados, sintaxe, inventário e preservação local. Sem iniciar o Minecraft."""
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
import tomllib
import zipfile

from gameplay_data import expected, DATA

ROOT = Path(__file__).resolve().parents[2]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    errors, known = [], []
    counts = dict(json=0, toml=0, javascript=0, python=0, markdown_links=0, preserved_gameplay_files=0, moved_files=0)
    paths = [p for p in (ROOT/'modpack').rglob('*') if p.is_file()
             and not any(part in {'.cache', 'build', 'showdown', '.sable', 'downloads', 'dynamic-data-pack-cache', 'dynamic-resource-pack-cache', '__pycache__'} for part in p.parts)]
    paths += list((ROOT/'extras').glob('*.md')) + [ROOT/f for f in ['README.md', 'AGENTS.md', 'ALTERACOES.md', 'TESTES.md']]
    for p in paths:
        relative = p.relative_to(ROOT).as_posix()
        try:
            if p.suffix in {'.json', '.mcmeta'}:
                try:
                    json.loads(p.read_text(encoding='utf-8-sig'))
                    counts['json'] += 1
                except ValueError as exc:
                    old = subprocess.run(['git', 'show', 'HEAD:'+relative], cwd=ROOT, capture_output=True)
                    if old.returncode == 0 and old.stdout.replace(b'\r\n', b'\n') == p.read_bytes().replace(b'\r\n', b'\n'):
                        known.append(relative+': formato legado preservado; '+str(exc))
                    else:
                        raise
            elif p.suffix == '.toml':
                tomllib.loads(p.read_text(encoding='utf-8-sig')); counts['toml'] += 1
            elif p.suffix == '.py':
                compile(p.read_text(encoding='utf-8'), str(p), 'exec'); counts['python'] += 1
            elif p.suffix == '.js':
                subprocess.run(['node', '--check', str(p)], check=True, capture_output=True); counts['javascript'] += 1
            elif p.suffix == '.md':
                for target in re.findall(r'\]\(([^)]+)\)', p.read_text(encoding='utf-8')):
                    if re.match(r'\w+://|mailto:|#', target):
                        continue
                    target = target.split('#')[0]
                    if target and not (p.parent/target).exists():
                        errors.append(relative+': link inexistente '+target)
                    counts['markdown_links'] += 1
        except Exception as exc:
            errors.append(relative+': '+str(exc))
    try:
        outputs, report = expected()
        for relative, value in outputs.items():
            assert json.loads((DATA/relative).read_text(encoding='utf-8')) == value, relative
        assert json.loads((ROOT/'modpack/gameplay-structure-manifest.json').read_text(encoding='utf-8')) == report
        manifest = json.loads((ROOT/'modpack/mods-manifest.json').read_text(encoding='utf-8'))
        assert sha(ROOT/'modpack/mods.zip') == manifest['archive']['sha256']
        with zipfile.ZipFile(ROOT/'modpack/mods.zip') as archive:
            actual = {n for n in archive.namelist() if n.lower().endswith('.jar')}
            assert actual == {e['file'] for e in manifest['entries']}
            for entry in manifest['entries']:
                assert hashlib.sha256(archive.read(entry['file'])).hexdigest() == entry['sha256']
        lines = (ROOT/'modpack/mods-list.txt').read_text(encoding='utf-8').splitlines()
        assert {line.split(' | ')[0] for line in lines if line and not line.startswith('#')} == actual
        assert not manifest['duplicate_top_level_ids'] and not manifest['duplicate_sha256']
        moves = json.loads((ROOT/'extras/reorganizacao.json').read_text(encoding='utf-8'))['moves']
        for move in moves:
            p = ROOT/move['to']
            assert p.exists(), move['to']
            hashes = move.get('sha256_before')
            # Fontes/documentos movidos receberam correções de caminhos intencionais.
            if isinstance(hashes, str) and p.suffix not in {'.md', '.json', '.py', '.java'}:
                assert sha(p) == hashes, move['to']
            if p.suffix == '.zip':
                with zipfile.ZipFile(p) as archive:
                    assert archive.testzip() is None
                if 'archive_sha256' in move:
                    assert sha(p) == move['archive_sha256']
            counts['moved_files'] += 1
        tracked = subprocess.check_output(['git', 'ls-tree', '-r', '--name-only', 'HEAD', 'modpack/kubejs', 'modpack/config', 'modpack/defaultconfigs'], cwd=ROOT, text=True).splitlines()
        intentional = {'modpack/config/worldgenrouter/world_sets.toml', 'modpack/config/sodium-fingerprint.json'}
        for relative in tracked:
            if relative in intentional:
                continue
            p = ROOT/relative
            assert p.exists(), 'Arquivo funcional desapareceu: '+relative
            old = subprocess.check_output(['git', 'show', 'HEAD:'+relative], cwd=ROOT)
            assert p.read_bytes().replace(b'\r\n', b'\n') == old.replace(b'\r\n', b'\n'), 'Arquivo funcional alterado fora do escopo: '+relative
            counts['preserved_gameplay_files'] += 1
        assert not (ROOT/'docs').exists() and not (ROOT/'tools').exists() and not (ROOT/'integration').exists()
        assert not any(p.is_dir() for p in (ROOT/'extras').iterdir()), 'extras deve ser plana'
        assert "base: '/Minecraft-2'" in (ROOT/'site/astro.config.mjs').read_text(encoding='utf-8')
        assert not subprocess.check_output(['git', 'diff', 'HEAD', '--name-only', '--', 'site', '.github'], cwd=ROOT).strip(), 'Site/workflows alterados'
    except Exception as exc:
        errors.append(type(exc).__name__+': '+str(exc))
    result = dict(passed=not errors, counts=counts, errors=errors, preserved_legacy_formats=known,
                  scope='Validação estática; não aprova os testes de cliente ou compatibilidade dos 122 JARs.')
    (ROOT/'extras/validacao-repositorio.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if errors:
        sys.exit(1)


if __name__ == '__main__':
    main()
