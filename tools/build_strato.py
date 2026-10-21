"""Build local do fork preparado, usando as bibliotecas Mojmap já instaladas.

Alternativa ao Gradle upstream; não modifica bibliotecas ou baixa dependências.
python tools/build_strato.py --jdk JDK --client-jar CLIENT_SRG_JAR
As bibliotecas NeoForge 21.1.263 ficam em modpack/.cache/server/libraries.
"""
import argparse
import hashlib
from pathlib import Path
import os
import re
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jdk', type=Path, required=True)
    parser.add_argument('--client-jar', type=Path, required=True)
    args = parser.parse_args()
    source=ROOT/'modpack/.cache/strato-m2'
    libs=[p for p in sorted((ROOT/'modpack/.cache/server/libraries').rglob('*.jar')) if not p.name.endswith(('-slim.jar','-unpacked.jar'))]
    libs.sort(key=lambda p: (0 if p.name=='neoforge-21.1.263-server.jar' else 1,str(p)))
    assert any(p.name=='neoforge-21.1.263-universal.jar' for p in libs)
    with zipfile.ZipFile(ROOT/'modpack/mods.zip') as z:
        name=next(n for n in z.namelist() if 'TerraBlender-neoforge-1.21.1-4.1.0.8.jar' in n)
        tb=source/'build/compile-only/terrablender.jar';tb.parent.mkdir(parents=True,exist_ok=True);tb.write_bytes(z.read(name))
    classes=source/'build/classes-m2';classes.mkdir(parents=True,exist_ok=True)
    cp=os.pathsep.join(str(p.resolve()) for p in [*libs,args.client_jar,tb])
    sources=sorted((source/'src/main/java').rglob('*.java'))
    # O mixin global foi removido da configuração e não deve ser embarcado.
    sources=[p for p in sources if '/mixin/' not in p.as_posix()]
    argv=['--release','21','-proc:none','-encoding','UTF-8','-classpath',cp,'-d',str(classes),*map(str,sources)]
    argfile=source/'build/javac-m2.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in argv),encoding='utf-8')
    subprocess.run([str(args.jdk/'bin/javac.exe' if os.name=='nt' else args.jdk/'bin/javac'),'@'+str(argfile)],check=True)
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
