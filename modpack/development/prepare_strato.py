"""Prepara um fork local isolado de Stratospherical 0.4.1 e seus dados Arcanus.

python modpack/development/prepare_strato.py --source modpack/.cache/strato-source
Compile a cópia em modpack/.cache/strato-m2 com Gradle/JDK 21.
Após compilar: repita com --apply para instalar somente os JSONs próprios de integração.
O fonte upstream e o JAR gerado permanecem fora do Git.
"""
import argparse
import json
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PIN = 'e5da51aabda9801885a15a446001e159411a63ea'
JAVA = Path('src/main/java/com/Apothic0n/StratosphericalExpansion')


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    entries = value.get('generator',{}).get('biome_source',{}).get('biomes') if isinstance(value,dict) else None
    if entries and len(entries)>1000:
        value=json.loads(json.dumps(value))
        value['generator']['biome_source']['biomes']='__M2_BIOME_ENTRIES__'
    text=json.dumps(value,ensure_ascii=False,indent=2)
    if entries and len(entries)>1000:
        text=text.replace('"__M2_BIOME_ENTRIES__"','[\n'+',\n'.join('        '+json.dumps(e,separators=(',',':')) for e in entries)+'\n      ]')
    path.write_text(text+'\n',encoding='utf-8')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source', required=True, type=Path)
    parser.add_argument('--apply', action='store_true')
    parser.add_argument('--output', type=Path, help='Cópia de build nova, dentro de modpack/.cache.')
    parser.add_argument('--staging', type=Path, help='Dados intermediários, dentro de modpack/.cache.')
    parser.add_argument('--vanilla', type=Path, default=ROOT/'modpack/.cache/server/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-extra.jar')
    args = parser.parse_args()
    source = args.source.resolve()
    assert subprocess.check_output(['git','-C',str(source),'rev-parse','HEAD'],text=True).strip()==PIN, 'Fonte não corresponde ao commit auditado.'
    assert not subprocess.check_output(['git','-C',str(source),'status','--porcelain'],text=True).strip(), 'Use um checkout upstream limpo.'
    out = (args.output or ROOT/'modpack/.cache/strato-m2').resolve()
    staging = (args.staging or ROOT/'modpack/.cache/strato-data').resolve()
    for destination in (out, staging):
        if not destination.is_relative_to((ROOT/'modpack/.cache').resolve()) or destination == (ROOT/'modpack/.cache').resolve():
            parser.error('Saídas devem ficar dentro de modpack/.cache, nunca na instância.')
    if not args.apply:
        assert staging.resolve().is_relative_to((ROOT/'modpack/.cache').resolve())
        if staging.exists():shutil.rmtree(staging)
        shutil.copytree(source,out,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build'))
        main_java = out/JAVA/'StratosphericalExpansion.java'
        text = main_java.read_text()
        call = 'SurfaceRuleManager.setDefaultSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, StratoSurfaceRuleData.makeRules());'
        assert call in text
        main_java.write_text(text.replace(call,'// Minecraft 2: a surface_rule integral fica no noise settings exclusivo de Arcanus.'),encoding='utf-8')
        mixin = out/'src/main/resources/mixins.strato.json'
        value = json.loads(mixin.read_text());value['mixins']=[];write(mixin,value)
        utils = out/JAVA/'api/biome/features/types/StratoDripstoneUtils.java'
        text = utils.read_text()
        assert 'DripstoneUtils::isEmptyOrWaterOrLava' in text
        utils.write_text(text.replace('DripstoneUtils::isEmptyOrWaterOrLava','state -> DripstoneUtils.isEmptyOrWaterOrLava(state) || state.is(net.minecraft.world.level.block.Blocks.FROSTED_ICE)'),encoding='utf-8')
        own_tag = 'net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.ResourceLocation.parse("strato:arcanus_base_stone"))'
        text = utils.read_text().replace('\n    private StratoDripstoneUtils()', '\n    static boolean isDripstoneBaseOrLava(net.minecraft.world.level.block.state.BlockState state) {\n        return DripstoneUtils.isDripstoneBaseOrLava(state) || state.is('+own_tag+');\n    }\n\n    private StratoDripstoneUtils()')
        utils.write_text(text,encoding='utf-8')
        for java in (out/JAVA/'api/biome/features/types').glob('*.java'):
            text=java.read_text().replace('BlockTags.BASE_STONE_OVERWORLD',own_tag).replace('DripstoneUtils::isDripstoneBaseOrLava','StratoDripstoneUtils::isDripstoneBaseOrLava')
            java.write_text(text,encoding='utf-8')
        # Retira os overrides globais da cópia de build, mantendo o upstream intacto.
        vanilla = out/'src/main/resources/data/minecraft'
        stone_tag=json.loads((vanilla/'tags/blocks/base_stone_overworld.json').read_text())
        stone_tag['values'].insert(0,'#minecraft:base_stone_overworld')
        write(out/'src/main/resources/data/strato/tags/block/arcanus_base_stone.json',stone_tag)
        for resource in (out/'src/main/resources/data/strato').rglob('*.json'):
            if resource.name != 'arcanus_base_stone.json':
                resource.write_text(resource.read_text().replace('minecraft:base_stone_overworld','strato:arcanus_base_stone'),encoding='utf-8')
        dimension = json.loads((vanilla/'dimension/overworld.json').read_text())
        biome_names = {p.stem for p in (vanilla/'worldgen/biome').glob('*.json')}
        biome_names |= {e['biome'].split(':')[1] for e in dimension['generator']['biome_source']['biomes']}

        def remap(value):
            if isinstance(value,list):return [remap(x) for x in value]
            if not isinstance(value,dict):return value
            result = {k:remap(v) for k,v in value.items()}
            if result.get('type') == 'minecraft:biome' and 'biome_is' in result:
                result['biome_is'] = ['m2arcana:strato/'+b.split(':')[1] if b.startswith('minecraft:') and b.split(':')[1] in biome_names else b for b in result['biome_is']]
            if 'biome' in result and isinstance(result['biome'],str) and result['biome'].startswith('minecraft:'):
                result['biome'] = 'm2arcana:strato/'+result['biome'].split(':')[1]
            return result

        # Resolve dependências vanilla em um namespace próprio. Terralith altera
        # density functions, noises, carvers e features vanilla globalmente.
        # Copia somente o fecho de dependências realmente alcançado pelo Strato.
        candidates={}
        with zipfile.ZipFile(args.vanilla) as jar:
            for name in jar.namelist():
                if not name.startswith('data/minecraft/worldgen/') or not name.endswith('.json'):continue
                registry, resource=name[len('data/minecraft/worldgen/'): -5].split('/',1)
                if registry in {'density_function','noise','placed_feature','configured_feature','configured_carver'}:
                    candidates.setdefault('minecraft:'+resource,[]).append((registry,resource,json.loads(jar.read(name))))
        terrain_allowed=set()
        feature_allowed=set()
        # Toda dependência de terreno; features/carvers somente quando há colisão.
        for key, nodes in candidates.items():
            if any(n[0] in {'density_function','noise'} for n in nodes):terrain_allowed.add(key)
        with zipfile.ZipFile(ROOT/'modpack/mods.zip') as archive:
            name=next(n for n in archive.namelist() if 'Terralith_' in n)
            import io
            with zipfile.ZipFile(io.BytesIO(archive.read(name))) as jar:
                for name in jar.namelist():
                    if name.startswith('data/minecraft/worldgen/') and name.endswith('.json'):
                        registry,resource=name[len('data/minecraft/worldgen/'):-5].split('/',1)
                        if registry in {'placed_feature','configured_feature','configured_carver'}:feature_allowed.add('minecraft:'+resource)
        pending=set()

        def isolate(value,terrain=True):
            allowed=terrain_allowed if terrain else feature_allowed
            if isinstance(value,str) and value in allowed and value in candidates:
                pending.add((terrain,value))
                return 'm2arcana:vanilla/'+value.split(':')[1]
            if isinstance(value,list):return [isolate(v,terrain) for v in value]
            if isinstance(value,dict):return {k:(v if k in {'type','Name','block','blocks','fluid','fluids','tag'} else isolate(v,terrain)) for k,v in value.items()}
            return value

        for p in (vanilla/'worldgen/biome').glob('*.json'):
            write(staging/'m2arcana/worldgen/biome/strato'/p.name,isolate(remap(json.loads(p.read_text())),False))
        # O upstream referencia beach/deep_lukewarm_ocean sem sobrescrevê-los.
        # Clonar também esses biomas mantém toda a paleta válida e isolada.
        with zipfile.ZipFile(args.vanilla) as jar:
            for name in sorted(biome_names):
                if not (vanilla/'worldgen/biome'/f'{name}.json').exists():
                    write(staging/'m2arcana/worldgen/biome/strato'/f'{name}.json',isolate(remap(json.loads(jar.read(f'data/minecraft/worldgen/biome/{name}.json'))),False))
        for resource in (out/'src/main/resources/data/strato').rglob('*.json'):
            write(resource,isolate(json.loads(resource.read_text()),False))
        dimension = isolate(remap(dimension))
        visited=set()
        while pending-visited:
            terrain,key=sorted(pending-visited)[0];visited.add((terrain,key))
            for registry,resource,value in candidates[key]:
                if (registry in {'density_function','noise'})==terrain:
                    write(staging/'m2arcana/worldgen'/registry/'vanilla'/f'{resource}.json',isolate(value,terrain))
        write(staging/'m2arcana/worldgen/noise_settings/strato.json',dimension['generator']['settings'])
        dimension['generator']['settings']='m2arcana:strato'
        # Mantém a floresta de archwood como parte da faixa temperada da distribuição.
        archwood = 0
        for entry in dimension['generator']['biome_source']['biomes']:
            humidity = entry['parameters']['humidity']
            h = humidity[0] if isinstance(humidity,list) else humidity
            if entry['biome']=='m2arcana:strato/forest' and h>=0:
                entry['biome']='m2arcana:ars_nouveau/archwood_forest';archwood+=1
        assert archwood, 'Revisar parâmetros para manter o bioma mágico.'
        write(staging/'m2arcana/dimension/world.json',dimension)
        # Tags legadas (blocks -> block) e de estruturas ficam limitadas aos clones.
        for p in (vanilla/'tags').rglob('*.json'):
            if p.name=='base_stone_overworld.json':continue
            value=json.loads(p.read_text())
            value['replace']=False
            value['values']=['m2arcana:strato/'+v.split(':')[1] if isinstance(v,str) and v.startswith('minecraft:') and v.split(':')[1] in biome_names else v for v in value['values']]
            rel=p.relative_to(vanilla).as_posix().replace('tags/blocks/','tags/block/')
            write(staging/'minecraft'/rel,value)
        (staging/'minecraft/tags/block/base_stone_overworld.json').unlink(missing_ok=True)
        # Somente a cópia de build ignorada é removida; nenhum arquivo original do repo.
        assert vanilla.resolve().is_relative_to((ROOT/'modpack/.cache').resolve())
        shutil.rmtree(vanilla)
        prop=out/'gradle.properties'
        prop.write_text(prop.read_text().replace('mod_version=0.4.1','mod_version=0.4.1-m2.1'),encoding='utf-8')
        print('Fonte preparado:',out,'; entradas archwood:',archwood)
    else:
        jar=out/'build/libs/strato-0.4.1-m2.1.jar'
        assert jar.is_file(), 'Compile o fork antes de ativar recursos que dependem dele.'
        data=ROOT/'modpack/kubejs/data'
        for p in staging.rglob('*.json'):
            target=data/p.relative_to(staging)
            # Tags do Minecraft devem acrescentar, não substituir integrações existentes.
            value=json.loads(p.read_text())
            if target.exists() and '/tags/' in target.as_posix():
                old=json.loads(target.read_text())
                value['values']=list(dict.fromkeys(old['values']+value['values']))
            write(target,value)
        tag=data/'m2arcana/tags/worldgen/biome/dimension_biomes.json'
        value=json.loads(tag.read_text())
        value['values']=sorted(set(value['values'])|{'m2arcana:strato/'+p.stem for p in (staging/'m2arcana/worldgen/biome/strato').glob('*.json')})
        write(tag,value)
        print('JSONs Arcanus ativados no repo. Instale o JAR local em cópia de teste e regenere o preset.')


if __name__=='__main__':main()
