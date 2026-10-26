"""Prepara/executa auditoria destrutiva SOMENTE em servidor descartável no cache.

Instale antes o servidor oficial 21.1.263 no diretório --server e compile --tests.
Não aceita instâncias externas nem saves preexistentes sem marcador próprio.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import shutil
import subprocess
import time
import tomllib
import zipfile
from build_support import ROOT, CACHE, jdk_home

# Exclusões explícitas do servidor dedicado; nunca alteram mods.zip/perfil cliente.
CLIENT_PREFIXES = ('appleskin-', 'better-advanced-tooltips-', 'BetterF3-', 'chat_heads-',
    'chunksfadein-', 'entityculling-', 'entity_pin_cushions-', 'fancymenu_', 'ImmediatelyFast-',
    'iris-', 'irisblockcompat-', 'Jade-', 'jei-', 'konkrete_', 'melody_', 'moreculling-',
    'musictriggers-', 'sodium-', 'xaeroworldmap-', 'ProbeJS-')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server', type=Path, default=CACHE/'toroidal-server')
    parser.add_argument('--strato', type=Path, default=CACHE/'strato-m2/build/libs/strato-0.4.1-m2.1.jar')
    parser.add_argument('--seed', type=int, default=420211)
    parser.add_argument('--preset', choices=('minecraft:normal','wover:normal','m2worlds:toroidal'), default='minecraft:normal',
                        help='Testa o preset normal por padrão, sem depender de escolha manual do preset M2.')
    parser.add_argument('--reopen', action='store_true')
    parser.add_argument('--suite',choices=('topology','regression','gameplay','biomes'),default='topology')
    parser.add_argument('--prepare-only', action='store_true')
    args = parser.parse_args()
    server = args.server.resolve()
    if not server.is_relative_to(CACHE.resolve()) or server == CACHE.resolve():
        parser.error('Servidor deve estar em subpasta de modpack/.cache.')
    launch = server/'libraries/net/neoforged/neoforge/21.1.263/win_args.txt'
    if not launch.is_file():
        parser.error('Instale o servidor oficial NeoForge 21.1.263 nesse diretório primeiro.')
    marker=server/'m2-disposable-audit.json'
    if (server/'audit-world').exists() and not marker.is_file():
        parser.error('Save sem marcador próprio: recusado.')
    if args.suite=='topology' and not args.reopen and (server/'audit-world').exists():
        parser.error('Mundo já existe; use --reopen ou outro diretório de servidor novo.')
    if args.reopen and (not marker.is_file() or not (server/'audit-world').exists()):
        parser.error('Execute a fase write antes da reabertura.')
    if args.reopen or marker.is_file():
        manifest=json.loads(marker.read_text())
        if manifest['seed'] != args.seed:
            parser.error('Seed diverge da fase write.')
        if manifest.get('preset','m2worlds:toroidal') != args.preset:
            parser.error('Preset diverge da fase write; informe --preset correspondente.')
        for name,digest in manifest['inputs'].items():
            # NeoForge/mods reformatam COMMON e completam defaults no primeiro boot.
            # JARs e datapack precisam ser idênticos; configs observadas vão ao resultado.
            if name.startswith(('mods/','kubejs/','defaultconfigs/')) and sha(server/name)!=digest:
                parser.error('Entrada mudou entre boots: '+name)
    else:
        if (server/'mods').exists() and list((server/'mods').iterdir()):
            parser.error('Pasta mods já contém arquivos; escolha servidor novo.')
        mods=server/'mods';mods.mkdir(exist_ok=True)
        excluded=[]
        archive=ROOT/'modpack/mods.zip'
        with zipfile.ZipFile(archive) as z:
            for name in z.namelist():
                if not name.endswith('.jar'): continue
                basename=Path(name).name
                if basename.startswith(CLIENT_PREFIXES):
                    excluded.append(name);continue
                data=z.read(name)
                with zipfile.ZipFile(io.BytesIO(data)) as jar:
                    metadata=tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode('utf-8-sig')) if 'META-INF/neoforge.mods.toml' in jar.namelist() else {}
                if any(mod.get('modId')=='strato' for mod in metadata.get('mods',[])):
                    excluded.append(name);continue
                (mods/basename).write_bytes(data)
        for source in (args.strato, ROOT/'modpack/integration/build/m2-worldgen-1.1.0.jar',
                       ROOT/'modpack/integration/build/m2-worldgen-tests-1.1.0.jar'):
            shutil.copyfile(source,mods/source.name)
        for directory in ('config','defaultconfigs','kubejs'):
            shutil.copytree(ROOT/'modpack'/directory,server/directory,dirs_exist_ok=True)
        (server/'server.properties').write_text(
            'level-name=audit-world\nlevel-type='+args.preset.replace(':','\\:')+'\nlevel-seed='+str(args.seed)+
            '\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25579\nview-distance=2\n'
            'simulation-distance=2\nmax-tick-time=-1\nspawn-protection=0\n',encoding='utf-8')
        inputs={p.relative_to(server).as_posix():sha(p) for directory in ('mods','config','defaultconfigs','kubejs')
                for p in (server/directory).rglob('*') if p.is_file()}
        manifest=dict(seed=args.seed,preset=args.preset,archive_sha256=sha(archive),excluded=excluded,inputs=inputs,
            note='Servidor dedicado descartável; exclusões de cliente e substituição única Strato; não certifica cliente completo.')
        marker.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    if args.prepare_only:
        print('Preparado:',server);return
    # O operador deve aceitar o EULA oficial antes de executar o servidor.
    if not (server/'eula.txt').is_file() or 'eula=true' not in (server/'eula.txt').read_text():
        parser.error('Servidor preparado. Leia/aceite o EULA Minecraft em eula.txt para iniciar.')
    jdk=jdk_home()
    phase=('reopen' if args.reopen else 'write') if args.suite=='topology' else args.suite
    result_path=server/('m2-topology-'+phase+'.json' if args.suite=='topology' else
        'm2-arcanus-atlas.json' if args.suite=='biomes' else
        'm2-gameplay-audit.json' if args.suite=='gameplay' else 'm2-worldgen-audit.json')
    if result_path.exists():
        parser.error('Resultado já existe; preserve-o e use um servidor novo para outra execução.')
    command=[str(jdk/'bin/java.exe'),'-Xmx6G',
        '-Dm2.audit.topologyOnly='+str(args.suite=='topology').lower(),
        '-Dm2.audit.biomesOnly='+str(args.suite=='biomes').lower(),
        '-Dm2.audit.denseAtlas='+str(args.suite=='topology' and not args.reopen).lower(),
        '-Dm2.audit.gameplayOnly='+str(args.suite=='gameplay').lower(),
        '-Dm2.audit.reopen='+str(args.reopen).lower(),'@'+str(launch),'nogui']
    terminated_after_save=False
    with (server/('audit-'+phase+'.log')).open('w',encoding='utf-8') as log:
        process=subprocess.Popen(command,cwd=server,stdout=log,stderr=subprocess.STDOUT)
        deadline=time.monotonic()+1800
        while process.poll() is None:
            try:
                process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                with (server/('audit-'+phase+'.log')).open('rb') as stream:
                    stream.seek(max(0,stream.seek(0,2)-65536))
                    tail=stream.read().decode('utf-8',errors='replace')
                # Sable pode manter threads nativas após o fechamento completo do save.
                # Finaliza somente o filho deste runner, após a confirmação vanilla.
                stopping=tail.rfind('Stopping server')
                saved=tail.rfind('ThreadedAnvilChunkStorage: All dimensions are saved')
                if stopping>=0 and saved>stopping and result_path.is_file():
                    terminated_after_save=True
                    process.terminate();process.wait(timeout=30)
                elif time.monotonic()>deadline:
                    process.terminate();process.wait(timeout=30)
                    raise RuntimeError('Timeout de 30 minutos; processo próprio encerrado, resultado não aprovado.')
    if not result_path.is_file():
        raise RuntimeError('Servidor não concluiu auditoria; confira audit-'+phase+'.log; exit='+str(process.returncode))
    report=json.loads(result_path.read_text())
    report['inputManifestSha256']=sha(marker)
    report['observed_config_sha256']={p.relative_to(server).as_posix():sha(p)
        for p in sorted((server/'config').rglob('*')) if p.is_file()}
    report['serverProcessExitCode']=process.returncode
    report['terminatedOwnProcessAfterConfirmedSave']=terminated_after_save
    final_log=(server/('audit-'+phase+'.log')).read_text(encoding='utf-8',errors='replace')
    report['shutdownSaveConfirmed']=final_log.rfind('ThreadedAnvilChunkStorage: All dimensions are saved')>final_log.rfind('Stopping server')>=0
    report['logSha256']=sha(server/('audit-'+phase+'.log'))
    # Resultado emitido no mesmo boot, sem aproveitar JSON residual.
    final_marker='[M2 ATLAS] Resultado: ' if args.suite=='biomes' else '[M2 TOPOLOGY] Resultado: ' if args.suite=='topology' else '[M2 GAMEPLAY] Resultado' if args.suite=='gameplay' else '[M2 AUDIT] Resultado'
    if final_marker not in (server/('audit-'+phase+'.log')).read_text(encoding='utf-8',errors='replace'):
        raise RuntimeError('Resultado sem marcador final no log desse boot.')
    result_path.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(result_path, 'passed=',report['passed'])
    if not report['passed']: raise SystemExit(1)


if __name__=='__main__':
    main()
