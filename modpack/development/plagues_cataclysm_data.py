"""M2-028/029: gera/confere overrides a partir dos JARs locais, sem iniciar Minecraft.

Spore não consta do mods.zip histórico: é lido somente no perfil indicado.
Não copia JARs terceiros nem renova a aprovação de inventários antigos.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / 'modpack/kubejs/data'


def sha(content):
    return hashlib.sha256(content).hexdigest()


def expected(profile):
    outputs = {}
    with zipfile.ZipFile(ROOT / 'modpack/mods.zip') as archive:
        cat_name = next(n for n in archive.namelist() if 'Cataclysm' in n and n.endswith('.jar'))
        payload = archive.read(cat_name)
        with zipfile.ZipFile(io.BytesIO(payload)) as cat:
            structures = sorted(Path(n).stem for n in cat.namelist() if n.startswith('data/cataclysm/worldgen/structure/') and n.endswith('.json'))
            for n in cat.namelist():
                if n.startswith('data/cataclysm/worldgen/structure_set/') and n.endswith('.json'):
                    value = json.loads(cat.read(n))
                    value['placement']['frequency'] = 0.0
                    outputs[n.removeprefix('data/')] = value
        cat_hash = sha(payload)
        flesh_name = next(n for n in archive.namelist() if Path(n).name.startswith('TFTH-') and n.endswith('.jar'))
        with zipfile.ZipFile(io.BytesIO(archive.read(flesh_name))) as flesh:
            for n in flesh.namelist():
                if n.startswith('data/the_flesh_that_hates/worldgen/structure/') and n.endswith('.json'):
                    value = json.loads(flesh.read(n))
                    assert value['biomes'] == 'minecraft:mushroom_fields'
                    value['biomes'] = ['m2arcana:strato/mushroom_fields']
                    outputs[n.removeprefix('data/')] = value
    spore_path = next((profile / 'mods').glob('spore_1.21.1_2.2.0j_neo.jar'))
    with zipfile.ZipFile(spore_path) as spore:
        def clone(selector):
            if isinstance(selector, list):
                return sorted({item for value in selector for item in clone(value)})
            if selector.startswith('#'):
                ns, path = selector[1:].split(':')
                return clone(json.loads(spore.read(f'data/{ns}/tags/worldgen/biome/{path}.json'))['values'])
            ns, path = selector.split(':')
            result = f'm2pokemon:{ns}/{path}'
            assert (DATA / f'm2pokemon/worldgen/biome/{ns}/{path}.json').is_file(), result
            return [result]
        for n in spore.namelist():
            if n.startswith('data/spore/worldgen/structure/') and n.endswith('.json'):
                value = json.loads(spore.read(n)); value['biomes'] = clone(value['biomes'])
                outputs[n.removeprefix('data/')] = value
            elif n == 'data/spore/neoforge/biome_modifier/ground_fungal_foliage_modifier.json':
                value = json.loads(spore.read(n)); value['biomes'] = clone(value['biomes'])
                outputs[n.removeprefix('data/')] = value
    return outputs, dict(cataclysm_jar=Path(cat_name).name, cataclysm_sha256=cat_hash,
                         spore_jar=spore_path.name, spore_sha256=sha(spore_path.read_bytes()),
                         unique_structures=structures)


def inventory():
    """Novo inventário por hash; não certifica licença/compatibilidade/distribuição."""
    path = ROOT / 'modpack/mods.zip'
    jars = []
    with zipfile.ZipFile(path) as archive:
        for n in sorted(archive.namelist()):
            if not n.endswith('.jar'): continue
            payload = archive.read(n)
            with zipfile.ZipFile(io.BytesIO(payload)) as jar:
                meta = next((p for p in ('META-INF/neoforge.mods.toml', 'META-INF/mods.toml') if p in jar.namelist()), None)
                info = tomllib.loads(jar.read(meta).decode()) if meta else {}
                jars.append(dict(name=Path(n).name, sha256=sha(payload),
                                 mods=[dict(id=m['modId'], version=m.get('version')) for m in info.get('mods', [])],
                                 declared_license=info.get('license')))
    return dict(zip_sha256=sha(path.read_bytes()), count=len(jars), jars=jars,
                state='Inventário somente. Sem aprovação de compatibilidade ou redistribuição. Spore vem do perfil, não deste ZIP.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile', type=Path, default=Path.home() / 'curseforge/minecraft/Instances/M2D')
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--audit-output', type=Path)
    args = parser.parse_args()
    outputs, inputs = expected(args.profile)
    for relative, value in outputs.items():
        path = DATA / relative
        if args.check:
            assert json.loads(path.read_text(encoding='utf-8')) == value, relative
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    if args.audit_output:
        if args.audit_output.exists(): raise RuntimeError('Não sobrescreve auditoria existente')
        report = dict(inputs=inputs, zip_inventory=inventory(),
                      generated_files={str(DATA.relative_to(ROOT) / name): sha((DATA/name).read_bytes()) for name in sorted(outputs)},
                      runtime_tests='Não executados; solicitado pelo usuário')
        args.audit_output.write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    print(f'{len(outputs)} overrides conferidos/gerados; {len(inputs["unique_structures"])} IDs Cataclysm únicos.')


if __name__ == '__main__':
    main()
