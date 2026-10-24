"""Compila a integração própria contra bibliotecas de um servidor NeoForge 21.1.263.

Detecta o JDK 21+ real e reutiliza bibliotecas exatas do CurseForge (somente leitura).
Uso: python modpack/development/build_integration.py --jdk "C:/Program Files/Java/jdk-25.0.2"
JDK 21+; --release 21. Dependências ausentes vão somente para .cache; não altera a instância.
"""
import argparse
from pathlib import Path
import zipfile
from build_support import jdk_home, curseforge_libraries, server_libraries, mod_apis, compile_java

ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jdk', type=Path)
    parser.add_argument('--libraries', type=Path, help='Bibliotecas de servidor; sem esta opção usa CurseForge 1.21.1/21.1.263.')
    parser.add_argument('--install', type=Path, help='Diretório Install do CurseForge, somente leitura.')
    parser.add_argument('--tests', action='store_true', help='Compila somente o JAR separado de testes, que encerra o servidor.')
    args = parser.parse_args()
    jdk = jdk_home(args.jdk)
    libraries = server_libraries(args.libraries) if args.libraries else (curseforge_libraries(args.install) if args.install else curseforge_libraries())
    kind = 'test' if args.tests else 'main'
    libraries += mod_apis(('portals-', 'kubejs-', 'rhino-', 'exotic_worlds-', 'worldgenrouter-'))
    if args.tests:
        libraries += [ROOT/'modpack/integration/build/m2-worldgen-1.1.0.jar']
    build = ROOT/'modpack/integration/build'
    build.mkdir(parents=True, exist_ok=True)
    sources = sorted((ROOT/('modpack/integration/src/'+kind+'/java')).rglob('*.java'))
    out = compile_java(jdk, libraries, sources, build, build/('javac-'+kind+'.args'))
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
