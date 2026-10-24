"""Compila e audita os dois JARs; entrega local, sem instalar ou iniciar Minecraft.

Uso na raiz: python modpack/development/build_delivery.py
Exige Python 3.11+, Git, CurseForge 1.21.1/NeoForge 21.1.263 e mods.zip.
"""
import argparse
from collections import defaultdict
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import uuid
import zipfile

from audit_mods import inspect_jar, flatten
from build_support import ROOT, CACHE, INSTALL, jdk_home, curseforge_libraries
from prepare_strato import PIN

URL = 'https://github.com/Kamilhik/Stratospherical-Expansion-NeoForge'
BUILD = ROOT / 'modpack/integration/build'


def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def snapshot():
    result = {}
    for directory in ('config', 'defaultconfigs', 'kubejs', 'integration/src'):
        for path in sorted((ROOT / 'modpack' / directory).rglob('*')):
            if path.is_file() and not any(part in {'logs', '__pycache__'} for part in path.parts):
                result[path.relative_to(ROOT).as_posix()] = sha(path)
    return result


def run(script, *args):
    subprocess.run([sys.executable, str(ROOT / 'modpack/development' / script), *map(str, args)], check=True, cwd=ROOT)


def duplicates(entries):
    ids = defaultdict(list)
    for entry in entries:
        for mod in entry['mods']:
            ids[mod['modId']].append(entry['file'])
    return {key: value for key, value in ids.items() if len(value) > 1}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jdk', type=Path)
    parser.add_argument('--install', type=Path, default=INSTALL)
    parser.add_argument('--source', type=Path, default=CACHE/'strato-source')
    parser.add_argument('--output', type=Path, help='Diretório novo dentro de modpack/integration/build.')
    parser.add_argument('--instance-mods', type=Path, default=Path.home()/'curseforge/minecraft/Instances/M2D/mods', help='Somente leitura; inventário adicional da instância.')
    args = parser.parse_args()
    label = datetime.now().strftime('%Y%m%d-%H%M%S') + '-' + uuid.uuid4().hex[:6]
    output = (args.output or BUILD / ('entrega-' + label)).resolve()
    if not output.is_relative_to(BUILD.resolve()) or output == BUILD.resolve() or output.exists():
        parser.error('--output deve ser uma pasta nova dentro de modpack/integration/build.')
    before = snapshot()
    archive = ROOT / 'modpack/mods.zip'
    archive_hash = sha(archive)
    jdk = jdk_home(args.jdk)
    libraries = curseforge_libraries(args.install)
    source = args.source.resolve()
    if not source.exists():
        source.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(['git', 'clone', URL, str(source)], check=True)
        subprocess.run(['git', '-C', str(source), 'checkout', PIN], check=True)
    # Nunca troca o commit de um checkout existente do usuário.
    commit = subprocess.check_output(['git', '-C', str(source), 'rev-parse', 'HEAD'], text=True).strip()
    if commit != PIN or subprocess.check_output(['git', '-C', str(source), 'status', '--porcelain']).strip():
        raise RuntimeError('Fonte Strato deve estar limpo e exatamente no commit ' + PIN)
    prepared = CACHE / ('strato-build-' + label)
    staging = CACHE / ('strato-data-' + label)
    vanilla = args.install / 'versions/1.21.1/1.21.1.jar'
    run('prepare_strato.py', '--source', source, '--vanilla', vanilla, '--output', prepared, '--staging', staging)
    # Não chama --apply, generate_worldgen ou staging de perfil: dados existentes são preservados.
    run('build_strato.py', '--jdk', jdk, '--install', args.install, '--source', prepared)
    run('build_integration.py', '--jdk', jdk, '--install', args.install)
    artifacts = [BUILD/'m2-worldgen-1.1.0.jar', prepared/'build/libs/strato-0.4.1-m2.1.jar']
    own = [inspect_jar(p.read_bytes(), p.name) for p in artifacts]
    own_classes = set()
    expected = [('m2_worldgen', '1.1.0'), ('strato', '0.4.1-m2.1')]
    for path, entry, (mod_id, version) in zip(artifacts, own, expected):
        if [(m['modId'], m['version']) for m in entry['mods']] != [(mod_id, version)] or entry['loader'] != 'javafml':
            raise RuntimeError('Metadata inesperado: ' + path.name)
        with zipfile.ZipFile(path) as jar:
            names = jar.namelist()
            if jar.testzip() is not None or len(names) != len(set(names)):
                raise RuntimeError('ZIP inválido ou entradas duplicadas: ' + path.name)
            classes = [jar.read(n) for n in names if n.endswith('.class')]
            own_classes.update(n for n in names if n.endswith('.class'))
            if not classes or any(int.from_bytes(c[6:8], 'big') != 65 for c in classes):
                raise RuntimeError('Bytecode deve ser Java 21: ' + path.name)
            if mod_id == 'strato':
                if any(n.startswith('data/minecraft/') or '/mixin/' in n for n in names):
                    raise RuntimeError('Fork contém overrides ou mixin global')
                if json.loads(jar.read('mixins.strato.json'))['mixins']:
                    raise RuntimeError('Mixin global ativo')
                if jar.read('LICENSE') != (ROOT/'modpack/integration/third-party/strato-LICENSE.txt').read_bytes():
                    raise RuntimeError('Licença Strato divergente')
    with zipfile.ZipFile(archive) as z:
        entries = [inspect_jar(z.read(n), n) for n in sorted(z.namelist()) if n.endswith('.jar')]
    previous = json.loads((ROOT/'modpack/mods-manifest.json').read_text(encoding='utf-8'))
    inventory_matches = archive_hash == previous['archive']['sha256'] and {e['file']:e['sha256'] for e in entries} == {e['file']:e['sha256'] for e in previous['entries']}
    # A entrega contém só os próprios. Simula a composição substituindo todo mod de ID strato.
    excluded = [e for e in entries if any(m['modId'] == 'strato' for m in e['mods'])]
    collisions = []
    import io
    with zipfile.ZipFile(archive) as z:
        for entry in entries:
            if entry in excluded:
                continue
            with zipfile.ZipFile(io.BytesIO(z.read(entry['file']))) as jar:
                collisions.extend(dict(file=entry['file'], java_class=n) for n in sorted(own_classes.intersection(jar.namelist())))
    if collisions:
        raise RuntimeError('Classes das integrações colidem com outros mods: ' + str(collisions))
    composed = [e for e in entries if e not in excluded] + own
    duplicate_ids = duplicates(composed)
    provided = {m['modId'] for e in flatten(composed) for m in e['mods']} | {'minecraft', 'neoforge', 'java'}
    missing = []
    for e in flatten(composed):
        if e['loader'] == 'fabric':
            continue
        for deps in e['dependencies'].values():
            for dep in deps:
                if dep.get('type', 'required' if dep.get('mandatory', True) else 'optional') == 'required' and dep['modId'] not in provided:
                    missing.append({'file': e['file'], 'dependency': dep['modId']})
    # Conflitos de terceiros bloqueiam instalação, mas não impedem entregar os JARs compilados.
    instance_entries = [inspect_jar(p.read_bytes(), p.name) for p in sorted(args.instance_mods.glob('*.jar'))] if args.instance_mods.is_dir() else []
    if snapshot() != before or sha(archive) != archive_hash:
        raise RuntimeError('Dados/fontes ou mods.zip mudaram durante a compilação')
    output.mkdir(parents=True)
    report = dict(schema=1, state='compiled_and_statically_checked', source_commit=PIN,
                  source_url=URL, jdk_release=(jdk/'release').read_text().splitlines()[0],
                  archive_sha256=archive_hash, inventory_matches_existing_audit=inventory_matches,
                  archive_count=len(entries), artifacts=own, excluded_from_composition=excluded,
                  duplicate_top_level_ids=duplicate_ids, missing_required_ids=missing,
                  own_class_collisions_with_top_level_mods=collisions,
                  installation_blocked=bool(duplicate_ids or missing),
                  instance_audit=dict(count=len(instance_entries), duplicate_top_level_ids=duplicates(instance_entries),
                                      strato=[e for e in instance_entries if any(m['modId']=='strato' for m in e['mods'])]),
                  preserved_input_sha256=before,
                  libraries=[dict(file=p.name, sha256=sha(p)) for p in libraries],
                  client_tests='pending', runtime_tests='not_run', active_instance_modified=False)
    audit = output/'auditoria.json'
    audit.write_text(json.dumps(dict(entries=composed), ensure_ascii=False, indent=2), encoding='utf-8')
    cp = os.pathsep.join(str(p) for p in libraries if p.name in {'gson-2.10.1.jar', 'maven-artifact-3.8.5.jar', 'commons-lang3-3.14.0.jar'})
    subprocess.run([str(jdk/'bin/java.exe' if os.name == 'nt' else jdk/'bin/java'), '--class-path', cp, str(ROOT/'modpack/development/CheckModVersions.java'), str(audit)], check=True)
    report['declared_range_conflicts'] = json.loads(audit.read_text(encoding='utf-8'))['declaredRangeConflicts']
    (output/'inventario-zip.json').write_text(json.dumps(dict(archive_sha256=archive_hash, entries=entries), ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    (output/'inventario-M2D.json').write_text(json.dumps(dict(entries=instance_entries), ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    history = json.loads((ROOT/'modpack/integration-manifest.json').read_text(encoding='utf-8'))
    report['historical_hash_matches'] = {e['file']: any(h['file']==e['file'] and h['sha256']==e['sha256'] for h in history['artifacts']) for e in own}
    for path in artifacts:
        shutil.copyfile(path, output/path.name)
        if sha(output/path.name) != sha(path):
            raise RuntimeError('Cópia de entrega divergente')
    shutil.copyfile(ROOT/'modpack/integration/third-party/strato-LICENSE.txt', output/'strato-LICENSE.txt')
    # Entrega completa solicitada; exclui somente caches/logs/exports gerados.
    for directory in ('config', 'defaultconfigs', 'kubejs'):
        shutil.copytree(ROOT/'modpack'/directory, output/directory,
            ignore=shutil.ignore_patterns('logs','__pycache__','exported','downloads',
                'dynamic-data-pack-cache','dynamic-resource-pack-cache','generated',
                'dimension-history.json','generated-resource-index.json','validation-report.json','*.log'))
    report['delivered_overrides_sha256'] = {
        p.relative_to(output).as_posix(): sha(p)
        for directory in ('config','defaultconfigs','kubejs')
        for p in sorted((output/directory).rglob('*')) if p.is_file()}
    (output/'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    (output/'SHA256SUMS.txt').write_text(''.join(e['sha256']+'  '+e['file']+'\n' for e in own), encoding='utf-8')
    instructions = '''# Minecraft 2 — entrega local das integrações

Alvo: Minecraft Java 1.21.1 / NeoForge 21.1.263 / Java 21.
Este comando executa compilação e inspeção estática. Resultados de servidor, quando anexados, têm relatórios e hashes próprios; não aprovam cliente/gameplay além dos critérios efetivamente executados.

1. Crie uma cópia descartável do perfil e faça backup do save. Não instale na instância ativa.
2. Na cópia, retire da pasta mods qualquer JAR que declare o ID `strato` (incluindo upstream 0.4.6), bem como versões anteriores de m2_worldgen. Guarde-os fora de mods.
3. Copie somente m2-worldgen-1.1.0.jar e strato-0.4.1-m2.1.jar desta pasta para mods. Nunca instale os dois Strato simultaneamente.
4. Copie config, defaultconfigs e kubejs desta entrega para a cópia descartável do perfil. Seus hashes estão em delivered_overrides_sha256 no build-report.json. Mantenha os demais mods do perfil; são necessários BBL Portals 1.2.2, TerraBlender 4.1.0.8, Exotic Worlds 1.1.4 e dependências do modpack. Esta entrega não contém esses mods de terceiros.
5. Confira SHA256SUMS.txt com Get-FileHash -Algorithm SHA256. Reinicie completamente e crie mundo novo com preset m2worlds:toroidal. Não converta saves pessoais.
6. Antes de iniciar, resolva os IDs duplicados indicados abaixo na cópia de teste, mantendo somente uma versão compatível por ID. Não copie o ZIP inteiro sem conferir. Execute J01–J24 de TESTES.md. O JAR de testes que encerra o servidor NÃO faz parte desta entrega.

build-report.json registra bibliotecas, hashes, metadados e conflitos declarados. auditoria.json contém o inventário simulado do ZIP com o fork substituindo upstream, sem alterar o ZIP. As divergências de intervalos de terceiros continuam pendentes; compilação não aprova o conjunto em jogo ou sua distribuição. A instância M2D pode ter uma composição diferente do ZIP e requer nova auditoria na cópia de teste.

Strato: commit e5da51aabda9801885a15a446001e159411a63ea, patch prepare_strato.py existente, versão 0.4.1-m2.1, MIT; licença anexa. Código próprio sem licença pública definida. Entrega local, sem publicação.

Reproduzir na raiz: python modpack/development/build_delivery.py
Opções: --jdk CAMINHO_JDK --install CAMINHO_CURSEFORGE_INSTALL --source CHECKOUT_LIMPO_NO_COMMIT_FIXADO
'''
    instructions += '\nConflitos na composição do ZIP após substituir Strato:\n\n```json\n' + json.dumps(dict(duplicate_ids=duplicate_ids, missing_required_ids=missing), ensure_ascii=False, indent=2) + '\n```\n'
    (output/'INSTALACAO.md').write_text(instructions, encoding='utf-8')
    print('ENTREGA:', output)
    print('Hashes históricos:', report['historical_hash_matches'])


if __name__ == '__main__':
    main()
