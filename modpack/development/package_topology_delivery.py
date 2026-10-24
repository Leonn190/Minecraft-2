"""Empacota entrega já compilada, exigindo evidência dos mesmos JARs/overrides.

Não inicia jogo, não altera perfil ativo, não inclui o JAR da suíte de testes.
"""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import zipfile
from build_support import ROOT


def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--delivery',type=Path,required=True)
    parser.add_argument('--runtime',type=Path,required=True)
    parser.add_argument('--regression',type=Path,required=True)
    parser.add_argument('--atlas',type=Path,required=True)
    parser.add_argument('--evidence',type=Path,action='append',default=[])
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    folder=args.delivery.resolve();output=args.output.resolve()
    build_root=(ROOT/'modpack/integration/build').resolve()
    if not folder.is_relative_to(build_root) or not output.is_relative_to(build_root) or output.is_relative_to(folder):
        parser.error('Entrega e ZIP devem ficar dentro do build; ZIP fora da pasta empacotada.')
    if output.exists() or (folder/'relatorios').exists():parser.error('Preserve a entrega anterior; destinos devem ser novos.')
    build=json.loads((folder/'build-report.json').read_text(encoding='utf-8'))
    reports={name:json.loads(path.read_text(encoding='utf-8')) for name,path in
        [('topology',args.runtime),('regression',args.regression),('atlas',args.atlas)]}
    for name,report in reports.items():
        if not report.get('passed') or report.get('errors'):parser.error('Relatório não aprovado: '+name)
        inputs=report['evidence']['initial_inputs_sha256']
        for artifact in build['artifacts']:
            if sha(folder/artifact['file'])!=artifact['sha256'] or inputs['mods/'+artifact['file']]!=artifact['sha256']:
                parser.error('JAR não corresponde ao teste: '+artifact['file'])
        for relative,digest in build['delivered_overrides_sha256'].items():
            if sha(folder/relative)!=digest or inputs[relative]!=digest:
                parser.error('Override não corresponde ao teste: '+relative)
    if reports['atlas']['sampleCount']!=565504:parser.error('Atlas incompleto.')
    if {p.name for p in folder.rglob('*.jar')}!={a['file'] for a in build['artifacts']}:
        parser.error('JAR inesperado na entrega.')
    evidence=[args.runtime,args.regression,args.atlas,*args.evidence]
    unique={p.name:p for p in evidence}
    if len(unique)!=len(set(map(str,evidence))):parser.error('Nomes de evidência colidem.')
    if any(p.suffix.lower() not in {'.md','.json','.csv'} or not p.is_file() for p in unique.values()):
        parser.error('Evidência deve ser arquivo Markdown/JSON/CSV existente.')
    report_dir=folder/'relatorios';report_dir.mkdir()
    for source in unique.values():shutil.copyfile(source,report_dir/source.name)
    validation=dict(state='server_checks_passed_with_documented_limits',client='not_run',
        full_seam_gameplay='not_approved',all_51_biomes='not_approved',
        jar_and_override_hashes_match_all_three_executions=True,
        evidence_sha256={p.name:sha(p) for p in unique.values()})
    (folder/'runtime-validation.json').write_text(json.dumps(validation,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    manifest={p.relative_to(folder).as_posix():sha(p) for p in sorted(folder.rglob('*')) if p.is_file()}
    (folder/'MANIFESTO_COMPLETO.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(folder.rglob('*')):
            if path.is_file():archive.write(path,path.relative_to(folder).as_posix())
    with zipfile.ZipFile(output) as archive:
        assert archive.testzip() is None
        for relative,digest in manifest.items():assert hashlib.sha256(archive.read(relative)).hexdigest()==digest
    output.with_suffix('.sha256.txt').write_text(sha(output)+'  '+output.name+'\n',encoding='utf-8')
    print(output);print('SHA-256:',sha(output))


if __name__=='__main__':main()
