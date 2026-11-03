"""Inventário da pasta mods real, separado da auditoria do ZIP histórico; sem gameplay."""
import argparse
import collections
import hashlib
import json
from pathlib import Path
import zipfile
from audit_mods import inspect_jar, flatten

ROOT = Path(__file__).resolve().parents[2]


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def compare(before, after):
    old = {Path(e['file']).name: e['sha256'] for e in before}
    new = {Path(e['file']).name: e['sha256'] for e in after}
    by_id = lambda entries: {m['modId']: dict(file=Path(e['file']).name, version=m['version'])
                            for e in entries for m in e['mods']}
    old_ids, new_ids = by_id(before), by_id(after)
    return dict(previous_count=len(before), current_count=len(after),
        added_files=sorted(new.keys() - old.keys()), removed_files=sorted(old.keys() - new.keys()),
        changed_hash=sorted(n for n in old.keys() & new.keys() if old[n] != new[n]),
        added_mod_ids=sorted(new_ids.keys() - old_ids.keys()), removed_mod_ids=sorted(old_ids.keys() - new_ids.keys()),
        version_changes={k: dict(before=old_ids[k], after=new_ids[k]) for k in sorted(old_ids.keys() & new_ids.keys())
                         if old_ids[k]['version'] != new_ids[k]['version']})


def summarize(entries):
    ids = collections.defaultdict(list)
    all_entries = list(flatten(entries))
    for e in all_entries:
        for mod in e['mods']:
            ids[mod['modId']].append(dict(file=e['file'], version=mod['version']))
    provided = set(ids) | {'minecraft', 'neoforge', 'java'}
    missing = []
    for e in all_entries:
        if e['loader'] == 'fabric' or not isinstance(e['dependencies'], dict):
            continue
        for owner, deps in e['dependencies'].items():
            for dep in deps:
                required = dep.get('type', 'required' if dep.get('mandatory', True) else 'optional') == 'required'
                if required and dep['modId'] not in provided:
                    missing.append(dict(file=e['file'], owner=owner, dependency=dep))
    top = collections.defaultdict(list)
    for e in entries:
        for mod in e['mods']:
            top[mod['modId']].append(e['file'])
    hashes = collections.defaultdict(list)
    for e in entries:
        hashes[e['sha256']].append(e['file'])
    return dict(duplicate_top_level_ids={k:v for k,v in sorted(top.items()) if len(v)>1},
        repeated_ids_including_jarjar={k:v for k,v in sorted(ids.items()) if len(v)>1},
        duplicate_sha256={k:v for k,v in hashes.items() if len(v)>1}, missing_required_ids=missing)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile', type=Path, required=True)
    parser.add_argument('--report', type=Path, required=True, help='Novo relatório; nunca sobrescreve auditoria.')
    parser.add_argument('--previous', type=Path, help='Inventário histórico escolhido para a comparação.')
    args = parser.parse_args()
    args.report = args.report.resolve()
    if args.report.exists():
        raise RuntimeError('Escolha relatório novo; histórico preservado.')
    manifest = ROOT / 'modpack/mods-manifest.json'
    previous_bytes = (args.previous or manifest).read_bytes()
    previous = json.loads(previous_bytes)
    history = ROOT / ('extras/mods-manifest-historico-' + hashlib.sha256(previous_bytes).hexdigest()[:12] + '.json')
    if not history.exists():
        history.write_bytes(previous_bytes)
    entries = []
    for file in sorted((args.profile / 'mods').iterdir()):
        if not file.is_file() or '.jar' not in file.name.lower():
            continue
        e = inspect_jar(file.read_bytes(), file.name)
        e['state'] = 'active' if file.name.lower().endswith('.jar') else 'disabled'
        ids = {m['modId'] for m in e['mods']}
        e['category'] = 'integration' if ids & {'m2_worldgen', 'strato'} else 'external'
        if 'strato' in ids:
            e['category_note'] = 'Fork próprio de código de terceiros; licença original preservada.'
        entries.append(e)
    active = [e for e in entries if e['state'] == 'active']
    report = dict(schema=2, audited_on='2026-11-01', source='CurseForge/M2D/mods',
        target=dict(minecraft='1.21.1', loader='NeoForge', neoforge='21.1.263'),
        entries=entries, counts=dict(active=len(active), disabled=len(entries)-len(active),
            external=sum(e['category']=='external' for e in active), integrations=sum(e['category']=='integration' for e in active)),
        previous_audit_comparison=compare(previous['entries'], active),
        previous_manifest=dict(path=history.relative_to(ROOT).as_posix(), sha256=hashlib.sha256(previous_bytes).hexdigest()),
        **summarize(active),
        note='Ativo significa extensão carregável, não boot aprovado. Metadados não certificam gameplay ou redistribuição. Bibliotecas JarJar são discriminadas; não contadas como JARs principais.')
    archive = ROOT / 'modpack/mods.zip'
    if archive.exists():
        with zipfile.ZipFile(archive) as jar:
            packed = [inspect_jar(jar.read(n), n) for n in sorted(jar.namelist()) if n.lower().endswith('.jar')]
        zip_report = dict(schema=1, source='modpack/mods.zip', sha256=hashlib.sha256(archive.read_bytes()).hexdigest(),
            entries=packed, **summarize(packed), comparison_to_active_profile=compare(packed,active),
            note='ZIP histórico intacto; não determina os mods ativos e não foi aprovado para distribuição.')
        zip_path = args.report.with_name(args.report.stem + '-zip.json')
        if zip_path.exists():
            if json.loads(zip_path.read_text(encoding='utf-8')) != zip_report:
                raise RuntimeError('Relatório ZIP diferente já existe; não sobrescrever.')
        else:
            write_json(zip_path, zip_report)
        report['archive_audit'] = zip_path.relative_to(ROOT).as_posix()
    write_json(args.report, report)
    write_json(manifest, report)
    lines = ['# Minecraft 2 — inventário do perfil CurseForge M2D em 01/11/2026',
             '# Ativo = arquivo .jar; não equivale a compatibilidade em jogo ou aprovação de distribuição.',
             '# Arquivo | mod ID = versão | loader | estado | categoria | SHA-256']
    for e in entries:
        lines.append(' | '.join([e['file'], ', '.join(m['modId']+' = '+m['version'] for m in e['mods']),
                     str(e['loader']),e['state'],e['category'],e['sha256']]))
    (ROOT/'modpack/mods-list.txt').write_text('\n'.join(lines)+'\n', encoding='utf-8')
    print(json.dumps({k:v for k,v in report.items() if k not in ('entries', 'repeated_ids_including_jarjar')}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
