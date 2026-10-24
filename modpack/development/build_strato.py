"""Build local do fork preparado, usando as bibliotecas Mojmap já instaladas.

Alternativa ao Gradle upstream; reutiliza APIs exatas sem modificar a instância.
python modpack/development/build_strato.py --jdk JDK --client-jar CLIENT_SRG_JAR
As bibliotecas NeoForge 21.1.263 ficam em modpack/.cache/server/libraries.
"""
import argparse
import hashlib
from pathlib import Path
import zipfile
from build_support import jdk_home, curseforge_libraries, server_libraries, mod_apis, compile_java

ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jdk', type=Path)
    parser.add_argument('--client-jar', type=Path)
    parser.add_argument('--libraries', type=Path)
    parser.add_argument('--install', type=Path)
    parser.add_argument('--source', type=Path, default=ROOT/'modpack/.cache/strato-m2')
    args = parser.parse_args()
    source=args.source.resolve()
    jdk = jdk_home(args.jdk)
    libs = server_libraries(args.libraries) if args.libraries else (curseforge_libraries(args.install) if args.install else curseforge_libraries())
    if args.client_jar:
        libs.append(args.client_jar)
    libs += mod_apis(('TerraBlender-neoforge-1.21.1-4.1.0.8.jar',))
    build = source/'build'
    build.mkdir(parents=True, exist_ok=True)
    sources = sorted(p for p in (source/'src/main/java').rglob('*.java') if '/mixin/' not in p.as_posix())
    classes = compile_java(jdk, libs, sources, build, build/'javac-m2.args')
    properties=dict(line.split('=',1) for line in (source/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    jar=source/'build/libs/strato-0.4.1-m2.1.jar';jar.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(jar,'w',compression=zipfile.ZIP_DEFLATED) as z:
        for base in [classes,source/'src/main/resources']:
            for p in sorted(base.rglob('*')):
                if not p.is_file():continue
                relative=p.relative_to(base).as_posix()
                data=p.read_bytes()
                if relative in {'META-INF/neoforge.mods.toml','pack.mcmeta'}:
                    text=data.decode()
                    for k,v in properties.items():text=text.replace('${'+k+'}',v)
                    assert '${' not in text,relative
                    data=text.encode()
                info=zipfile.ZipInfo(relative,date_time=(2026,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,data)
        info=zipfile.ZipInfo('LICENSE',date_time=(2026,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED
        z.writestr(info, (source/'LICENSE').read_bytes())
    print(jar,hashlib.file_digest(jar.open('rb'),'sha256').hexdigest())


if __name__=='__main__':main()
