"""Compila a integração própria contra bibliotecas de um servidor NeoForge 21.1.263.

Instale o servidor em modpack/.cache/server com o instalador oficial antes.
Uso: python modpack/development/build_integration.py --jdk "C:/Program Files/Java/jdk-25.0.2"
JDK 21+; --release 21. Não baixa bibliotecas nem altera a instância do jogador.
"""
import argparse
import io
from pathlib import Path
import subprocess
import os
import zipfile

ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jdk', type=Path, required=True)
    parser.add_argument('--libraries', type=Path, default=ROOT/'modpack/.cache/server/libraries')
    parser.add_argument('--tests', action='store_true', help='Compila somente o JAR separado de testes, que encerra o servidor.')
    args = parser.parse_args()
    libraries = [p for p in sorted(args.libraries.rglob('*.jar')) if not p.name.endswith(('-slim.jar','-unpacked.jar'))]
    libraries.sort(key=lambda p: (0 if p.name=='neoforge-21.1.263-server.jar' else 1, str(p)))
    if not any(p.name == 'neoforge-21.1.263-universal.jar' for p in libraries):
        parser.error('Instale NeoForge 21.1.263 no diretório --libraries primeiro.')
    kind = 'test' if args.tests else 'main'
    # APIs exatas do ZIP auditado, sem depender de extrações manuais anteriores.
    api_dir = ROOT/'modpack/.cache/integration-apis'
    api_dir.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(ROOT/'modpack/mods.zip') as archive:
        for name in archive.namelist():
            if Path(name).name.startswith(('portals-', 'kubejs-', 'rhino-', 'exotic_worlds-', 'worldgenrouter-')) and name.endswith('.jar'):
                target = api_dir/Path(name).name
                target.write_bytes(archive.read(name))
                libraries.append(target)
                if target.name.startswith('exotic_worlds-'):
                    with zipfile.ZipFile(io.BytesIO(archive.read(name))) as bundle:
                        for nested in bundle.namelist():
                            if nested.startswith('META-INF/jarjar/exotic_worlds-neoforge-') and nested.endswith('.jar'):
                                engine=api_dir/Path(nested).name
                                engine.write_bytes(bundle.read(nested))
                                libraries.append(engine)
    if args.tests:
        libraries += [ROOT/'modpack/integration/build/m2-worldgen-1.1.0.jar']
    out = ROOT/('modpack/integration/build/classes-'+kind)
    out.mkdir(parents=True, exist_ok=True)
    cp = os.pathsep.join(str(p.resolve()) for p in libraries)
    sources = sorted((ROOT/('modpack/integration/src/'+kind+'/java')).rglob('*.java'))
    argfile = ROOT/'modpack/integration/build/javac.args'
    argv = ['--release', '21', '-proc:none', '-encoding', 'UTF-8', '-classpath', cp, '-d', str(out), *map(str, sources)]
    argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in argv), encoding='utf-8')
    subprocess.run([str(args.jdk/'bin/javac.exe' if os.name=='nt' else args.jdk/'bin/javac'), '@'+str(argfile)], check=True)
    jar = ROOT/('modpack/integration/build/m2-worldgen-'+('tests-' if args.tests else '')+'1.1.0.jar')
    with zipfile.ZipFile(jar, 'w', compression=zipfile.ZIP_DEFLATED) as z:
        for base in [out, ROOT/('modpack/integration/src/'+kind+'/resources')]:
            for p in sorted(base.rglob('*')):
                if p.is_file():
                    info = zipfile.ZipInfo(p.relative_to(base).as_posix(), date_time=(2026,1,1,0,0,0))
                    info.compress_type = zipfile.ZIP_DEFLATED
                    z.writestr(info,p.read_bytes())
    print(jar)


if __name__ == '__main__':
    main()
