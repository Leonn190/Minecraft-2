"""Gera filtros e preset a partir dos recursos reais, preservando geradores.

Uso: python modpack/development/generate_worldgen.py --vanilla CAMINHO_JAR
O JAR vanilla fornece as tags de bioma oficiais. Não instala na instância ativa.
"""
import argparse
import copy
import io
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT/'modpack/kubejs/data'
ADDONS = {'ars_additions', 'ars_structurize', 'ars_fauna'}


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    entries = value.get('generator',{}).get('biome_source',{}).get('biomes') if isinstance(value,dict) else None
    compact = entries and len(entries)>1000 and 'm2arcana' in path.parts
    if compact:
        value=copy.deepcopy(value)
        value['generator']['biome_source']['biomes']='__M2_BIOME_ENTRIES__'
    text=json.dumps(value,ensure_ascii=False,indent=2)
    if compact:
        text=text.replace('"__M2_BIOME_ENTRIES__"','[\n'+',\n'.join('        '+json.dumps(e,separators=(',',':')) for e in entries)+'\n      ]')
    path.write_text(text+'\n', encoding='utf-8')


def toroidal(stem, width):
    stem = copy.deepcopy(stem)
    generator = stem['generator']
    if generator['type'] not in {'minecraft:noise', 'exotic_worlds:toroidal', 'undergarden:noise', 'wover:betterx'}:
        raise ValueError('Gerador não suportado para conversão: '+generator['type'])
    assert width % 32 == 0 and 256 <= width <= 59999968
    radius = width//32
    bounds = {axis:dict(min_chunk=-radius, max_chunk=radius) for axis in ['x','z']}
    if generator['type'] in {'undergarden:noise', 'wover:betterx'}:
        # Exotic Worlds 1.0.0's StampedGeneratorCodec preserves custom subclasses.
        generator.update({'exotic_worlds:wrapping': bounds, 'exotic_worlds:climate_compression': False})
    else:
        generator.update(type='exotic_worlds:toroidal', wrapping=bounds, climate_compression=False)
    return stem


def write_normal_presets(preset):
    # A tag da tela só oferece o preset; não muda a escolha padrão vanilla/Wover.
    # Os dois IDs normais precisam carregar os mesmos três geradores toroidais.
    for namespace in ('minecraft', 'wover'):
        write(DATA/namespace/'worldgen/world_preset/normal.json', preset)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--vanilla', type=Path, required=True)
    args = parser.parse_args()
    tags = {}
    resources = {}

    def load(jar):
        for name in jar.namelist():
            if not name.endswith('.json') or not name.startswith('data/'):
                continue
            if '/tags/worldgen/biome/' in name:
                d = json.loads(jar.read(name).decode('utf-8-sig'))
                tag = name[5:-5].replace('/tags/worldgen/biome/', ':')
                tags[tag] = ([] if d.get('replace') else tags.get(tag, [])) + d['values']
            elif any(name.startswith('data/'+ns+'/') for ns in ADDONS) and '/tags/' not in name and ('/worldgen/structure/' in name or '/neoforge/biome_modifier/' in name):
                resources[name] = json.loads(jar.read(name).decode('utf-8-sig'))

    with zipfile.ZipFile(args.vanilla) as jar:
        load(jar)
    assert 'minecraft:is_overworld' in tags, 'O JAR --vanilla precisa conter os dados vanilla descompactados, não o bundler.'
    with zipfile.ZipFile(ROOT/'modpack/mods.zip') as archive:
        for name in sorted(archive.namelist()):
            if name.endswith('.jar'):
                with zipfile.ZipFile(io.BytesIO(archive.read(name))) as jar:
                    load(jar)
    for path in sorted(DATA.glob('*/tags/worldgen/biome/**/*.json')):
        tag = path.relative_to(DATA).as_posix()[:-5].replace('/tags/worldgen/biome/', ':')
        # Generated addon selectors below do not define tags; these are existing integrations.
        d = json.loads(path.read_text(encoding='utf-8-sig'))
        tags[tag] = ([] if d.get('replace') else tags.get(tag, [])) + d['values']

    def resolve(selector, seen=frozenset()):
        if isinstance(selector, list):
            return set().union(*(resolve(s, seen) for s in selector))
        if isinstance(selector, dict):
            selector = selector['id']
        if not selector.startswith('#'):
            return {selector if ':' in selector else 'minecraft:'+selector}
        tag = selector[1:]
        if tag in seen:
            return set()
        return set().union(*(resolve(s, seen | {tag}) for s in tags.get(tag, [])))

    aliases = {}
    for path in (DATA/'m2arcana/worldgen/biome').rglob('*.json'):
        relative = path.relative_to(DATA/'m2arcana/worldgen/biome').as_posix()[:-5]
        ns, biome = relative.split('/', 1)
        canonical = ('minecraft' if ns == 'strato' else ns)+':'+biome
        aliases.setdefault(canonical, set()).add('m2arcana:'+relative)
    counts = {}
    for name, value in sorted(resources.items()):
        if 'biomes' not in value:
            raise ValueError('Revisar recurso de geração sem seletor de biomas: '+name)
        selected = resolve(value['biomes'])
        targets = set()
        for biome in selected:
            if biome.startswith('m2arcana:'):
                targets.add(biome)
            targets.update(aliases.get(biome, set()))
        value['biomes'] = sorted(targets)
        write(DATA/name[5:], value)
        counts[name] = len(targets)

    worlds = {
        'base': ('minecraft:overworld', ['cobblemon', *sorted(ADDONS)]),
        'pokemon': ('m2pokemon:world', ['alexsmobs', *sorted(ADDONS)]),
        'arcanus': ('m2arcana:world', ['alexsmobs', 'cobblemon']),
        'undergarden': ('undergarden:undergarden', ['cobblemon', *sorted(ADDONS)]),
        'limbo': ('macabre:limbo', ['cobblemon', *sorted(ADDONS)]),
        'pit': ('macabre:the_pit', ['cobblemon', *sorted(ADDONS)]),
        'distortion': ('threateningly_mobs:distortion_dimension', ['cobblemon', *sorted(ADDONS)]),
    }
    lines = ['# Minecraft 2 — Worldgen Router 1.0.0', '# Somente geração natural. Sem política global de namespaces ou inventário.', '']
    for name, (dimension, deny) in worlds.items():
        if name!='arcanus':deny=[*deny,'strato']
        if name!='arcanus':deny=[*deny,'ars_nouveau','ars_elemental']
        if name not in {'pokemon','arcanus'}:deny=[*deny,'the_flesh_that_hates']
        header = 'world_sets.'+name
        lines += ['['+header+']', 'overworld = '+json.dumps(dimension), 'default = "allow"']
        if name == 'base':
            lines += ['nether = "minecraft:the_nether"', 'end = "minecraft:the_end"']
        lines += ['['+header+'.portal]', 'route_nether = false', 'exact_return = false']
        for category in ['structure', 'feature', 'carver', 'entity']:
            lines += ['['+header+'.rules.'+category+']', 'deny_namespaces = '+json.dumps(deny)]
            lines += ['apply_to = '+json.dumps(['natural_spawn'] if category == 'entity' else ['generation'])]
        for category in ['item', 'biome']:
            lines += ['['+header+'.rules.'+category+']', 'enabled = false']
        lines += ['']
    config = ROOT/'modpack/config/worldgenrouter/world_sets.toml'
    config.parent.mkdir(parents=True, exist_ok=True)
    config.write_text('\n'.join(lines), encoding='utf-8')
    wover = ROOT/'modpack/config/wover/main.json'
    w = json.loads(wover.read_text())
    w['server']['force_default_world_preset'] = False
    write(wover, w)

    # Each source is read from the existing integration or its actual installed JAR.
    stems = {}
    for dimension, width in [('minecraft:overworld',9984), ('minecraft:the_nether',4992), ('minecraft:the_end',6016), ('m2pokemon:world',4992), ('m2arcana:world',3008), ('undergarden:undergarden',4992), ('macabre:the_pit',2016)]:
        ns, name = dimension.split(':')
        source = DATA/ns/'dimension'/f'{name}.json'
        if source.exists():
            stem = json.loads(source.read_text())
        elif ns == 'minecraft':
            # Mesmo gerador/pickers do preset wover:normal já usado pelo perfil.
            # Conservar a subclasse é necessário para BetterEnd/BetterNether.
            end_or_nether = 'nether' if name=='the_nether' else 'end'
            stem = dict(type=dimension, generator=dict(type='wover:betterx', settings='minecraft:'+end_or_nether, biome_source=dict(type='wover:'+end_or_nether+'_biome_source',seed=0,config={})))
        else:
            with zipfile.ZipFile(ROOT/'modpack/mods.zip') as archive:
                prefix = {'undergarden':'The_Undergarden-', 'macabre':'macabre-'}[ns]
                filename = next(n for n in archive.namelist() if Path(n).name.startswith(prefix))
                with zipfile.ZipFile(io.BytesIO(archive.read(filename))) as jar:
                    stem = json.loads(jar.read(f'data/{ns}/dimension/{name}.json'))
        if ns=='minecraft':
            stems[dimension] = toroidal(stem, width)
        else:
            # Dimensões de datapack já entram no bake; declarar seus limites aqui
            # evita duplicar milhares de parâmetros climáticos dentro do preset.
            if ns in {'m2pokemon','m2arcana'}:
                stem['generator']['biome_source']['type']='m2_worldgen:isolated_climate'
            converted=toroidal(stem,width)
            # M2-025: modo STRONG oficial ajusta só o clima ao mapa de 3008 blocos.
            # Mantém as 7590 entradas/51 IDs, sem alterar pesos/faixas ou as outras dimensões.
            if ns=='m2arcana':converted['generator']['climate_compression']={'mode':'strong'}
            write(DATA/ns/'dimension'/f'{name}.json',converted)
    # 1:2 is a dimension-type property, not just the ratio between widths.
    with zipfile.ZipFile(args.vanilla) as jar:
        nether_type = json.loads(jar.read('data/minecraft/dimension_type/the_nether.json'))
    nether_type['coordinate_scale'] = 2.0
    write(DATA/'m2worlds/dimension_type/nether.json', nether_type)
    stems['minecraft:the_nether']['type'] = 'm2worlds:nether'
    write(DATA/'m2worlds/worldgen/world_preset/toroidal.json', dict(dimensions=stems))
    write_normal_presets(dict(dimensions=stems))
    write(DATA/'minecraft/tags/worldgen/world_preset/normal.json', dict(replace=False, values=['m2worlds:toroidal']))
    write(ROOT/'modpack/worldgen-selectors.json', dict(note='Seletores derivados dos JARs; zero significa habitat original sem equivalente no Arcanus.', resources=counts))
    print(f'{len(resources)} recursos de addons; sete dimensões com limites próprios, incluindo macabre:the_pit (Carne).')


if __name__ == '__main__':
    main()
