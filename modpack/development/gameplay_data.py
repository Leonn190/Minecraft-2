"""Gera/valida filtros contra os JARs exatos do ZIP. python modpack/development/gameplay_data.py [--check]"""
import argparse
import copy
import hashlib
import io
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT/'modpack/kubejs/data'
ALLOW = set(('aviary bandit_towers foundry heavenly_challenger infested_temple keep_kayra '
             'mechanical_nest mining_complex mining_system monastery plague_asylum shiraz_palace '
             'thornborn_towers kisegi_sanctuary').split())


def expected():
    outputs = {}
    with zipfile.ZipFile(ROOT/'modpack/mods.zip') as archive:
        name = next(n for n in archive.namelist() if Path(n).name.startswith('DungeonsArise-') and n.endswith('.jar'))
        payload = archive.read(name)
        with zipfile.ZipFile(io.BytesIO(payload)) as jar:
            structures = {Path(n).stem: (n, json.loads(jar.read(n))) for n in jar.namelist()
                          if n.startswith('data/dungeons_arise/worldgen/structure/') and n.endswith('.json')}
            assert ALLOW <= structures.keys(), 'Escolha ausente na versão instalada: '+str(ALLOW-structures.keys())
            for stem, (path, value) in structures.items():
                if stem in ALLOW:
                    assert not (DATA/Path(path).relative_to('data')).exists(), 'Override inesperado de estrutura permitida'
                    continue
                value = copy.deepcopy(value)
                value['biomes'] = '#m2worlds:disabled_structures'
                outputs[str(Path(path).relative_to('data'))] = value
            # Não altera placement/weights, nem cria novos candidatos de mineração.
            sets = {Path(n).stem: json.loads(jar.read(n)) for n in jar.namelist()
                    if n.startswith('data/dungeons_arise/worldgen/structure_set/') and n.endswith('.json')}
            for stem in sets:
                assert not (DATA/f'dungeons_arise/worldgen/structure_set/{stem}.json').exists()
        outputs['m2worlds/tags/worldgen/biome/disabled_structures.json'] = {'replace': True, 'values': []}
        flesh_name = next(n for n in archive.namelist() if Path(n).name.startswith('TFTH-') and n.endswith('.jar'))
        with zipfile.ZipFile(io.BytesIO(archive.read(flesh_name))) as flesh:
            for path in flesh.namelist():
                if path.startswith('data/the_flesh_that_hates/worldgen/structure/') and path.endswith('.json'):
                    value = json.loads(flesh.read(path))
                    assert value['biomes'] == 'minecraft:mushroom_fields', 'Habitat TFTH mudou; revisar remapeamento'
                    value['biomes'] = ['m2pokemon:minecraft/mushroom_fields', 'm2arcana:strato/mushroom_fields']
                    for biome in value['biomes']:
                        ns, stem = biome.split(':')
                        assert (DATA/f'{ns}/worldgen/biome/{stem}.json').is_file()
                    outputs[str(Path(path).relative_to('data'))] = value
        report = dict(schema=1, jar=name, sha256=hashlib.sha256(payload).hexdigest(),
                      allowed=sorted('dungeons_arise:'+s for s in ALLOW),
                      blocked=sorted('dungeons_arise:'+s for s in structures.keys()-ALLOW),
                      original_structure_sets=sets,
                      note='13 escolhas / 14 IDs. mining_system e giant_mushroom não constam dos structure sets originais; não são acrescentados.')
    return outputs, report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs, report = expected()
    for relative, value in outputs.items():
        path = DATA/relative
        if args.check:
            assert json.loads(path.read_text(encoding='utf-8')) == value, relative
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    target = ROOT/'modpack/gameplay-structure-manifest.json'
    if args.check:
        assert json.loads(target.read_text(encoding='utf-8')) == report
    else:
        target.write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(f"WDA: {len(report['allowed'])} IDs permitidos, {len(report['blocked'])} bloqueados; placement e pesos preservados.")


if __name__ == '__main__':
    main()
