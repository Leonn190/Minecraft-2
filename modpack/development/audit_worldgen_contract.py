"""Verifica contratos dos dados atuais sem fingir execução de Minecraft.

Não atualiza inventários/hashes históricos. Relatório novo em --output.
"""
import argparse
from collections import Counter
import hashlib
import io
import json
from pathlib import Path
import tomllib
import zipfile
from build_support import ROOT
from gameplay_data import expected

DATA=ROOT/'modpack/kubejs/data'
WIDTHS={'minecraft:overworld':9984,'minecraft:the_nether':4992,'minecraft:the_end':6016,
        'm2pokemon:world':4992,'m2arcana:world':3008,'undergarden:undergarden':4992,'macabre:the_pit':2016}


def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    if args.output.exists(): parser.error('Preserve relatórios anteriores: --output deve ser novo.')
    errors=[]
    def check(ok,message):
        if not ok: errors.append(message)
    preset=json.loads((DATA/'m2worlds/worldgen/world_preset/toroidal.json').read_text(encoding="utf-8"))
    for namespace in ('minecraft','wover'):
        normal=json.loads((DATA/namespace/'worldgen/world_preset/normal.json').read_text(encoding='utf-8'))
        check(normal==preset,'Preset normal deve manter os geradores toroidais: '+namespace)
    dims={}
    for id,width in WIDTHS.items():
        ns,name=id.split(':')
        stem=preset['dimensions'][id] if ns=='minecraft' else json.loads((DATA/ns/'dimension'/f'{name}.json').read_text(encoding="utf-8"))
        gen=stem['generator'];prefix='' if gen['type']=='exotic_worlds:toroidal' else 'exotic_worlds:'
        wrap=gen[prefix+'wrapping']
        check(all((wrap[a]['max_chunk']-wrap[a]['min_chunk'])*16==width for a in ('x','z')),'Largura '+id)
        dims[id]={'generator':gen['type'],'wrapping':wrap,'climate_compression':gen.get(prefix+'climate_compression')}
    nouveau=tomllib.loads((ROOT/'modpack/config/ars_nouveau-common.toml').read_text(encoding="utf-8"))
    elemental=tomllib.loads((ROOT/'modpack/config/ars_elemental-common.toml').read_text(encoding="utf-8"))
    check(nouveau['general']['archwoodForest']==0,'Peso TerraBlender Ars Nouveau')
    def find_key(d,key):
        if key in d:return d[key]
        for v in d.values():
            if isinstance(v,dict):
                found=find_key(v,key)
                if found is not None:return found
    check(find_key(elemental,'extra_biomes')==0,'Peso TerraBlender Ars Elemental')
    sets=tomllib.loads((ROOT/'modpack/config/worldgenrouter/world_sets.toml').read_text(encoding="utf-8"))['world_sets']
    for name,s in sets.items():
        for category in ('feature','structure','carver','entity'):
            namespaces=s['rules'][category]['deny_namespaces']
            check(all((ns in namespaces)==(name!='arcanus') for ns in ('ars_nouveau','ars_elemental')),'Router '+name+' '+category)
        check(not s['rules']['item']['enabled'],'Router não deve bloquear itens '+name)
    clone='m2arcana:ars_nouveau/archwood_forest'
    modifier=json.loads((DATA/'ars_elemental/neoforge/biome_modifier/common_flashing_archwood.json').read_text(encoding="utf-8"))
    check(modifier['biomes']==[clone],'Flashing comum deve manter habitat original clonado')
    arc=json.loads((DATA/'m2arcana/dimension/world.json').read_text(encoding="utf-8"))
    check(arc['generator']['climate_compression']=={'mode':'strong'},'Compressão climática STRONG Arcanus')
    entries=arc['generator']['biome_source']['biomes']
    palette=Counter(e['biome'] for e in entries)
    check(len(palette)==51 and len(entries)==7590,'Contrato paleta/clima Arcanus')
    outputs,gameplay_report=expected()
    for relative,value in outputs.items():
        check(json.loads((DATA/relative).read_text(encoding="utf-8"))==value,'Preservação gameplay '+relative)
    # Compara todos os overrides originais UG/Pit retirando apenas os campos EW.
    originals={}
    with zipfile.ZipFile(ROOT/'modpack/mods.zip') as z:
        for ns,prefix,name in [('undergarden','The_Undergarden-','undergarden'),('macabre','macabre-','the_pit')]:
            jarname=next(n for n in z.namelist() if Path(n).name.startswith(prefix))
            with zipfile.ZipFile(io.BytesIO(z.read(jarname))) as jar:
                original=json.loads(jar.read(f'data/{ns}/dimension/{name}.json'))
            actual=json.loads((DATA/ns/'dimension'/f'{name}.json').read_text(encoding="utf-8"))
            actual['generator']={k:v for k,v in actual['generator'].items() if not k.startswith('exotic_worlds:') and k not in ('wrapping','climate_compression')}
            # Pit é vanilla noise convertido para o codec toroidal equivalente.
            if actual['generator']['type']=='exotic_worlds:toroidal':actual['generator']['type']='minecraft:noise'
            originals[ns]=actual==original
            check(originals[ns],'Gerador/conteúdo original '+ns)
    report=dict(state='static_only',passed=not errors,errors=errors,archive_sha256=sha(ROOT/'modpack/mods.zip'),
        dimensions=dims,arcanus=dict(climate_entries=len(entries),palette_size=len(palette),
            entries_per_biome=dict(sorted(palette.items())),note='Contagem de entradas não é frequência espacial nem peso probabilístico.'),
        original_dimension_content_preserved=originals,gameplay_resources_checked=len(outputs),
        source_sha256={p.relative_to(ROOT).as_posix():sha(p) for folder in ('modpack/integration/src','modpack/development')
                       for p in sorted((ROOT/folder).rglob('*')) if p.is_file() and p.suffix in ('.py','.java','.json','.toml')},
        manual_client='not_run',fluids_entities_ticks_structures_maps='not_approved_by_static_checks')
    args.output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(args.output,'passed=',report['passed'],errors)
    if errors: raise SystemExit(1)


if __name__=='__main__':main()
