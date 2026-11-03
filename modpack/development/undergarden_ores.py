"""Produz somente os dois overrides de frequência a partir do JAR real (sem testes)."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile', required=True, type=Path)
    parser.add_argument('--count', default=4, type=int, help='Tentativas/chunk; original 3.')
    parser.add_argument('--report', type=Path, default=ROOT / 'extras/geracao-minerios-20261101.json',
                        help='Relatório novo para preservar o histórico ao ajustar --count.')
    args = parser.parse_args()
    if not 4 <= args.count <= 6:
        parser.error('Escolha moderada: de 4 a 6 tentativas por chunk.')
    output = args.report.resolve()
    if output.exists():
        raise RuntimeError('Relatório histórico já existe; use outro nome antes de repetir.')
    jars = sorted((args.profile / 'mods').glob('The_Undergarden-*.jar'))
    if len(jars) != 1:
        raise RuntimeError('Esperado exatamente um Undergarden ativo.')
    report = dict(source=jars[0].name, sha256=hashlib.sha256(jars[0].read_bytes()).hexdigest(), ores={})
    with zipfile.ZipFile(jars[0]) as jar:
        for ore in ('utherium', 'regalium'):
            name = f'data/undergarden/worldgen/placed_feature/{ore}_ore.json'
            data = json.loads(jar.read(name))
            counts = [p for p in data['placement'] if p['type'] == 'minecraft:count']
            if len(counts) != 1 or counts[0]['count'] != 3:
                raise RuntimeError('Baseline mudou; revisar antes de gerar: ' + name)
            configured = json.loads(jar.read(f'data/undergarden/worldgen/configured_feature/{ore}_ore.json'))
            original = json.loads(json.dumps(data))
            counts[0]['count'] = args.count
            target = ROOT / 'modpack/kubejs' / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
            report['ores'][ore] = dict(original=original, installed=data, configured_feature_unchanged=configured,
                attempts_increase_percent=(args.count / 3 - 1) * 100,
                note='Aumento de tentativas, não garantia de igual aumento de blocos; biomas/alturas preservados.')
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(output)


if __name__ == '__main__':
    main()
