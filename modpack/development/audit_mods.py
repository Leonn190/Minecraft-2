"""Inventário offline: lê JARs em memória, inclusive dependências Jar-in-Jar.

Uso: python modpack/development/audit_mods.py [--active DIRETORIO_MODS]
Não instala, modifica ou redistribui mods. Python 3.11+.
"""
import argparse
import collections
import hashlib
import io
import json
from pathlib import Path
import re
import os
import subprocess
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[2]


def inspect_jar(data, name, nested=False):
    with zipfile.ZipFile(io.BytesIO(data)) as jar:
        names = set(jar.namelist())
        manifest = jar.read('META-INF/MANIFEST.MF').decode('utf-8', 'replace') if 'META-INF/MANIFEST.MF' in names else ''
        manifest = manifest.replace('\r\n ', '').replace('\n ', '')
        version = re.search(r'^Implementation-Version: (.+)', manifest, re.M)
        metadata = next((p for p in ['META-INF/neoforge.mods.toml', 'META-INF/mods.toml', 'fabric.mod.json'] if p in names), None)
        result = dict(file=name, sha256=hashlib.sha256(data).hexdigest(), bytes=len(data), metadata=metadata, nested=nested)
        if metadata and metadata.endswith('.toml'):
            meta = tomllib.loads(jar.read(metadata).decode('utf-8-sig'))
            result.update(loader=meta.get('modLoader'), license=meta.get('license'), mods=meta.get('mods', []), dependencies=meta.get('dependencies', {}))
            result['mods'] = [{k: m[k] for k in ['modId', 'version', 'displayName', 'displayURL'] if k in m} for m in result['mods']]
            for mod in result['mods']:
                if mod.get('version') == '${file.jarVersion}':
                    mod['version'] = version.group(1).strip() if version else 'UNRESOLVED'
            result['source'] = meta.get('issueTrackerURL')
        elif metadata:
            meta = json.loads(jar.read(metadata))
            result.update(loader='fabric', license=meta.get('license'), mods=[dict(modId=meta['id'], version=meta['version'], displayName=meta.get('name'))], dependencies=meta.get('depends', {}))
        else:
            result.update(loader=None, mods=[], dependencies={})
        result['dimensions'] = sorted(n[5:-5].replace('/dimension/', ':') for n in names if re.fullmatch(r'data/[^/]+/dimension/[^/]+\.json', n))
        # NeoForge only loads declared JarJar children, not the Fabric bundle.
        children = []
        if 'META-INF/jarjar/metadata.json' in names:
            for child in json.loads(jar.read('META-INF/jarjar/metadata.json')).get('jars', []):
                p = child['path']
                children.append(inspect_jar(jar.read(p), name + '!/' + p, True))
        result['children'] = children
        return result


def flatten(entries):
    for entry in entries:
        yield entry
        yield from flatten(entry['children'])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--active', type=Path)
    parser.add_argument('--jdk', type=Path, help='Opcional: JDK para checar intervalos Maven com o helper Java.')
    args = parser.parse_args()
    archive = ROOT / 'modpack/mods.zip'
    if not archive.is_file():
        parser.error('ZIP ausente: modpack/mods.zip; nenhum inventário foi sobrescrito.')
    with zipfile.ZipFile(archive) as z:
        entries = [inspect_jar(z.read(n), n) for n in sorted(z.namelist()) if n.lower().endswith('.jar')]
    all_entries = list(flatten(entries))
    launcher = ROOT/'extras/minecraftinstance.json'
    if launcher.exists():
        addons = json.loads(launcher.read_text(encoding='utf-8-sig')).get('installedAddons', [])
        by_file = {a.get('fileNameOnDisk'):a for a in addons}
        for e in entries:
            addon=by_file.get(Path(e['file']).name)
            if addon:
                installed=addon.get('installedFile',{})
                e['origin'] = dict(evidence='minecraftinstance.json local; não é confirmação independente de distribuição', project_url=addon.get('webSiteURL'), download_url=installed.get('downloadUrl'), curseforge_project_id=addon.get('addonID'), curseforge_file_id=installed.get('id'))
    ids = collections.defaultdict(list)
    for entry in all_entries:
        for mod in entry['mods']:
            ids[mod['modId']].append(dict(file=entry['file'], version=mod['version']))
    provided = set(ids) | {'minecraft', 'neoforge', 'java'}
    missing = []
    for entry in all_entries:
        if isinstance(entry['dependencies'], dict) and entry['loader'] != 'fabric':
            for owner, deps in entry['dependencies'].items():
                for dep in deps:
                    if dep.get('type', 'required' if dep.get('mandatory', True) else 'optional') == 'required' and dep['modId'] not in provided:
                        missing.append(dict(file=entry['file'], owner=owner, dependency=dep))
    report = dict(schema=1, target=dict(minecraft='1.21.1', loader='NeoForge'), archive=dict(path='modpack/mods.zip', sha256=hashlib.file_digest(archive.open('rb'), 'sha256').hexdigest()), entries=entries,
                  duplicate_ids={k:v for k,v in ids.items() if len(v)>1}, missing_required_ids=missing,
                  note='Metadados declarados; não certifica intervalos de versão, compatibilidade em jogo ou redistribuição.')
    if args.active:
        active = {p.name: hashlib.file_digest(p.open('rb'), 'sha256').hexdigest() for p in args.active.glob('*.jar')}
        expected = {Path(e['file']).name:e['sha256'] for e in entries}
        report['active_comparison'] = dict(count=len(active), only_zip=sorted(expected.keys()-active.keys()), only_active=sorted(active.keys()-expected.keys()), different_hash=sorted(k for k in expected.keys() & active.keys() if expected[k]!=active[k]))
    previous_path = ROOT/'modpack/mods-manifest.json'
    previous = json.loads(previous_path.read_text(encoding='utf-8')) if previous_path.exists() else {}
    if previous.get('archive', {}).get('sha256') == report['archive']['sha256']:
        for key in ['active_comparison', 'declaredRangeConflicts', 'previous_audit_comparison']:
            if key in previous and key not in report:
                report[key] = previous[key]
        report['preserved_evidence'] = 'Evidências anteriores preservadas somente para o mesmo SHA-256; a auditoria do ZIP foi repetida, a comparação do perfil somente quando --active foi informado.'
    elif previous.get('entries') and previous.get('archive', {}).get('sha256'):
        history = ROOT/('extras/mods-manifest-'+previous['archive']['sha256'][:12]+'.json')
        if not history.exists():
            history.write_text(json.dumps(previous, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
        before = {e['file']: e['sha256'] for e in previous['entries']}
        after = {e['file']: e['sha256'] for e in entries}
        report['previous_audit_comparison'] = dict(archive_sha256=previous['archive']['sha256'],
            added=sorted(after.keys()-before.keys()), removed=sorted(before.keys()-after.keys()),
            changed_hash=sorted(k for k in before.keys() & after.keys() if before[k] != after[k]),
            evidence=history.relative_to(ROOT).as_posix()+'; resultados antigos não aprovam ZIP diferente')
    top_ids = collections.defaultdict(list)
    for entry in entries:
        for mod in entry['mods']:
            top_ids[mod['modId']].append(entry['file'])
    report['duplicate_top_level_ids'] = {k:v for k,v in top_ids.items() if len(v)>1}
    report['duplicate_sha256'] = {h:[e['file'] for e in entries if e['sha256']==h] for h in {e['sha256'] for e in entries} if sum(e['sha256']==h for e in entries)>1}
    report['launcher_comparison'] = dict(addons=len(addons) if launcher.exists() else None, only_zip=sorted(Path(e['file']).name for e in entries if Path(e['file']).name not in by_file) if launcher.exists() else [], only_launcher=sorted(set(by_file)-{Path(e['file']).name for e in entries}) if launcher.exists() else [])
    (ROOT/'modpack/mods-manifest.json').write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    if args.jdk:
        jars = list((ROOT/'modpack/.cache/server/libraries').rglob('*.jar'))
        cp = os.pathsep.join(str(p) for p in jars if p.name in {'gson-2.10.1.jar','maven-artifact-3.8.5.jar','commons-lang3-3.14.0.jar'})
        subprocess.run([str(args.jdk/('bin/java.exe' if os.name=='nt' else 'bin/java')), '--class-path',cp,str(ROOT/'modpack/development/CheckModVersions.java'),str(ROOT/'modpack/mods-manifest.json')],check=True)
    lines = ['# Minecraft 2 — inventário do ZIP, não lista de mods ativos', '# Gerado por modpack/development/audit_mods.py; versões declaradas no metadata do JAR.', '# Arquivo | mod ID = versão | loader']
    for e in entries:
        lines.append(e['file']+' | '+', '.join(m['modId']+' = '+m['version'] for m in e['mods'])+' | '+str(e['loader']))
    (ROOT/'modpack/mods-list.txt').write_text('\n'.join(lines)+'\n', encoding='utf-8')
    print(json.dumps({k:v for k,v in report.items() if k!='entries'},ensure_ascii=False,indent=2))


if __name__ == '__main__':
    main()
