"""Registra evidência concluída de servidor sem publicar logs ou dados pessoais.

Preserva JSONs anteriores. O mod de testes nunca integra a entrega do jogador.
"""
import argparse
import csv
import hashlib
import json
from pathlib import Path
from build_support import CACHE


def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server',type=Path,required=True)
    parser.add_argument('--phase',choices=('write','reopen','regression','biomes'),required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    server=args.server.resolve()
    if not server.is_relative_to(CACHE.resolve()) or args.output.exists():
        parser.error('Servidor deve estar no cache e saída deve ser nova.')
    raw=server/('m2-arcanus-atlas.json' if args.phase=='biomes' else 'm2-worldgen-audit.json' if args.phase=='regression' else 'm2-topology-'+args.phase+'.json')
    log=server/('audit-'+args.phase+'.log')
    if args.phase=='biomes' and not log.exists():log=server/'audit-write.log'
    report=json.loads(raw.read_text(encoding='utf-8'))
    text=log.read_text(encoding='utf-8',errors='replace')
    marker='[M2 ATLAS] Resultado: ' if args.phase=='biomes' else '[M2 AUDIT] Resultado: ' if args.phase=='regression' else '[M2 TOPOLOGY] Resultado: PASS'
    if not report.get('passed') or report.get('errors') or marker not in text:
        parser.error('Recusado: resultado não aprovado ou sem marcador final no log desse boot.')
    if args.phase!='biomes' and len(report['dimensions'])!=7:parser.error('Esperadas sete dimensões auditadas.')
    if args.phase=='biomes' and report.get('sampleCount')!=565504:parser.error('Atlas denso incompleto.')
    manifest=json.loads((server/'m2-disposable-audit.json').read_text(encoding='utf-8'))
    for name,digest in manifest['inputs'].items():
        if name.startswith(('mods/','kubejs/','defaultconfigs/')) and sha(server/name)!=digest:
            parser.error('Entrada diverge do boot: '+name)
    dimensions=[]
    spatial=[]
    for d in report.get('dimensions',[]):
        copy=dict(d)
        if 'generator' in copy:
            encoded=copy.pop('generator')
            copy['encodedGeneratorSha256']=hashlib.sha256(json.dumps(encoded,sort_keys=True).encode()).hexdigest()
            copy['encodedGeneratorType']=encoded.get('type')
            copy['runtimeWrapping']=encoded.get('wrapping',encoded.get('exotic_worlds:wrapping'))
            copy['runtimeClimateCompression']=encoded.get('climate_compression',encoded.get('exotic_worlds:climate_compression'))
        for sample in copy.pop('spatialSamplesY64',[]):
            spatial.append(sample)
        if args.phase=='reopen' and not all(p['persistedMarker'] for p in d['blockProbes']):
            parser.error('Marcadores não persistiram em '+d['id'])
        dimensions.append(copy)
    report['dimensions']=dimensions
    stopping=text.rfind('Stopping server');saved=text.rfind('ThreadedAnvilChunkStorage: All dimensions are saved')
    report['evidence']=dict(raw_json_sha256=sha(raw),log_sha256=sha(log),manifest_sha256=sha(server/'m2-disposable-audit.json'),
        seed=manifest['seed'],archive_sha256=manifest['archive_sha256'],
        shutdown_save_after_stopping_confirmed=stopping>=0 and saved>stopping,
        initial_inputs_sha256=manifest['inputs'],excluded_client_jars=manifest['excluded'],
        note='Apenas os critérios desta suíte foram aprovados; avisos/erros de recursos de terceiros podem existir no log; cliente não executado.')
    args.output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if spatial:
        csv_path=args.output.with_suffix('.csv')
        if csv_path.exists():parser.error('CSV já existe; preserve a execução anterior.')
        with csv_path.open('w',encoding='utf-8',newline='') as stream:
            writer=csv.writer(stream);writer.writerow(['x','z','biome_y64','base_surface_y','biome_base_surface'])
            writer.writerows([row+['',''] if len(row)==3 else row for row in spatial])
    print(args.output)


if __name__=='__main__':main()
