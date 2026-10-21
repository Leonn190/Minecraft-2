"""Monta overrides locais para copiar a um perfil de TESTE com os 111 mods auditados.

Não toca na instância ativa e não inclui o mod de testes que encerra o servidor.
python tools/stage_worldgen_profile.py
"""
import hashlib
import json
from pathlib import Path
import shutil

ROOT=Path(__file__).resolve().parents[1]


def main():
    destination=ROOT/'modpack/.cache/worldgen-overrides'
    jars=[ROOT/'integration/build/m2-worldgen-1.0.0.jar',ROOT/'modpack/.cache/strato-m2/build/libs/strato-0.4.1-m2.1.jar']
    for jar in jars:
        if not jar.is_file():raise SystemExit('Compile antes: '+str(jar))
    for name in ['config','defaultconfigs','kubejs']:
        shutil.copytree(ROOT/'modpack'/name,destination/name,dirs_exist_ok=True)
    (destination/'mods').mkdir(exist_ok=True)
    artifacts=[]
    for jar in jars:
        shutil.copyfile(jar,destination/'mods'/jar.name)
        artifacts.append(dict(file=jar.name,version='1.0.0' if jar.name.startswith('m2-') else '0.4.1-m2.1',sha256=hashlib.file_digest(jar.open('rb'),'sha256').hexdigest()))
    manifest=dict(schema=1,minecraft='1.21.1',neoforge='21.1.263',java_compile='Microsoft OpenJDK 21.0.12.1',
        source=dict(strato_url='https://github.com/Kamilhik/Stratospherical-Expansion-NeoForge',strato_commit='e5da51aabda9801885a15a446001e159411a63ea',strato_license='MIT; licença no JAR local e attribution em docs/VALIDACAO_WORLDGEN.md'),
        artifacts=artifacts,note='Complemento local ao ZIP inventariado; não representa nova lista de mods ativos nem aprovação de distribuição dos demais mods.')
    (ROOT/'modpack/integration-manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(destination)


if __name__=='__main__':main()
