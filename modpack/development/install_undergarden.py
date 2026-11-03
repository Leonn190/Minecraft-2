"""Instala os seis arquivos próprios desta entrega, com backup, sem tocar saves."""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import zipfile
from audit_mods import inspect_jar

ROOT = Path(__file__).resolve().parents[2]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def stopped():
    result = subprocess.run(['powershell', '-NoProfile', '-Command',
        "$ErrorActionPreference='Stop'; @(Get-Process | Where-Object { $_.ProcessName -in @('java','javaw') }).Count"],
        capture_output=True, text=True, check=True)
    if result.stdout.strip() != '0':
        raise RuntimeError('Processo Java ativo ou consulta inconclusiva; perfil não alterado.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile', type=Path, required=True)
    args = parser.parse_args()
    profile = args.profile.resolve()
    if profile.name != 'M2D' or not (profile/'minecraftinstance.json').is_file():
        raise RuntimeError('Destino não é o perfil M2D esperado.')
    stopped()
    ids = {}
    for jar in (profile/'mods').glob('*.jar'):
        entry = inspect_jar(jar.read_bytes(), jar.name)
        for mod in entry['mods']:
            if mod['modId'] in ids:
                raise RuntimeError('IDs principais duplicados; analisar antes de instalar: '+mod['modId'])
            ids[mod['modId']] = jar.name
        if 'm2-worldgen-tests' in jar.name:
            raise RuntimeError('JAR exclusivo de testes presente no perfil normal.')
    if ids.get('strato') != 'strato-0.4.1-m2.1.jar' or ids.get('m2_worldgen') != 'm2-worldgen-1.1.0.jar':
        raise RuntimeError('Integrações do perfil divergentes; não substituir cegamente.')
    relative = [
        'kubejs/server_scripts/backpack_progression.js',
        'kubejs/assets/sophisticatedbackpacks/lang/en_us.json',
        'kubejs/assets/sophisticatedbackpacks/lang/pt_br.json',
        'kubejs/data/undergarden/worldgen/placed_feature/utherium_ore.json',
        'kubejs/data/undergarden/worldgen/placed_feature/regalium_ore.json']
    mapping = [(ROOT/'modpack'/r,profile/r) for r in relative]
    mapping.append((ROOT/'modpack/integration/build/m2-worldgen-1.1.0.jar',profile/'mods/m2-worldgen-1.1.0.jar'))
    # Conclui preflight antes de criar qualquer arquivo no perfil.
    for source, target in mapping:
        if not source.is_file() or profile not in target.resolve().parents:
            raise RuntimeError('Fonte/destino inválido: '+str(source))
        if target.exists() and target.suffix != '.jar' and source.read_bytes() != target.read_bytes():
            raise RuntimeError('Arquivo novo já existe com alterações locais; mesclar antes: '+str(target))
    stamp = datetime.datetime.now().strftime('%Y%m%d-%H%M%S')
    backup = ROOT / f'extras/backup-m2d-undergarden-{stamp}.zip'
    old = {target.relative_to(profile).as_posix():target.read_bytes() for _,target in mapping if target.exists()}
    with zipfile.ZipFile(backup,'x',zipfile.ZIP_DEFLATED) as archive:
        for name,data in old.items():
            archive.writestr(name,data)
    with zipfile.ZipFile(backup) as archive:
        for name,data in old.items():
            if archive.read(name) != data:
                raise RuntimeError('Backup não corresponde ao original: '+name)
    stopped()
    installed = []
    for source,target in mapping:
        before = sha(target) if target.exists() else None
        target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(source,target)
        if sha(source) != sha(target):
            raise RuntimeError('Cópia divergente: '+str(target))
        installed.append(dict(source=source.relative_to(ROOT).as_posix(),target=target.relative_to(profile).as_posix(),
                              before_sha256=before,sha256=sha(target)))
    # Inspeção da baseline main: sem instalar índices, preferências ou arquivos locais.
    tracked = subprocess.check_output(['git','ls-files','modpack/config','modpack/defaultconfigs','modpack/kubejs'],
                                     cwd=ROOT,text=True).splitlines()
    baseline = []
    for name in tracked:
        source = ROOT/name
        target = profile/source.relative_to(ROOT/'modpack')
        state = 'absent_generated_cache' if 'search_index' in source.parts and not target.exists() else (
            'missing' if not target.exists() else 'identical' if sha(source)==sha(target) else 'local_difference_preserved')
        baseline.append(dict(file=name,state=state,repository_sha256=sha(source),
                             installed_sha256=sha(target) if target.exists() else None))
    report = dict(id='M2-030/031/032/033',profile='CurseForge/M2D',date='2026-11-01',
        base_main=subprocess.check_output(['git','rev-parse','main'],cwd=ROOT,text=True).strip(),
        java_processes_at_preflight=0,backup=dict(path=backup.relative_to(ROOT).as_posix(),sha256=sha(backup),
        entries={name:hashlib.sha256(data).hexdigest() for name,data in old.items()},roundtrip_equal=True),
        installed=installed,stable_baseline=baseline,
        saves_accessed=False,tests_run=False,production_compilation=True,
        restart='Completo: serializer e eventos Java; não basta /reload.',
        note='Nenhum arquivo local extra apagado; diferenças de preferências preservadas. Instalação por hash não aprova gameplay.')
    output = ROOT/f'extras/instalacao-undergarden-{stamp}.json'
    output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(output)


if __name__ == '__main__':
    main()
