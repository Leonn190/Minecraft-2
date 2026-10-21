"""Registra uma auditoria REAL concluída; rejeita relatório antigo ou execução com falhas.

python tools/record_worldgen_validation.py --log boot-portals-v3.txt
"""
import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server', type=Path, default=ROOT/'modpack/.cache/server')
    parser.add_argument('--log', required=True)
    args = parser.parse_args()
    log = (args.server/args.log).read_text(encoding='utf-8-sig', errors='replace')
    raw = json.loads((args.server/'m2-worldgen-audit.json').read_text(encoding='utf-8-sig'))
    results = [line.split('[M2 AUDIT] Resultado: ', 1)[1] for line in log.splitlines() if '[M2 AUDIT] Resultado: ' in line]
    if not results or json.loads(results[-1]) != raw:
        raise SystemExit('O relatório não corresponde à conclusão do log informado.')
    if not raw.get('passed') or raw.get('errors') or len(raw.get('dimensions', [])) != 7 or 'portals' not in raw:
        raise SystemExit('Auditoria incompleta ou com falhas; nenhum resultado de aprovação foi gravado.')
    router = json.loads((args.server/'worldgenrouter/validation-report.json').read_text(encoding='utf-8'))
    if not router.get('valid') or router.get('errors'):
        raise SystemExit('O Router reportou uma configuração inválida.')
    manifest = json.loads((ROOT/'modpack/integration-manifest.json').read_text(encoding='utf-8'))
    for artifact in manifest['artifacts']:
        if sha(args.server/'mods'/artifact['file']) != artifact['sha256']:
            raise SystemExit('Manifesto diverge do JAR testado: '+artifact['file'])
    properties = dict(line.split('=', 1) for line in (args.server/'server.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    java = re.search(r'JVM identified as ([^\r\n]+)', log)
    raw['execution'] = dict(recorded_utc=datetime.now(timezone.utc).isoformat(), minecraft='1.21.1', neoforge='21.1.263',
        java_runtime=java.group(1).strip() if java else 'não identificado no log', world=properties['level-name'], seed=int(properties['level-seed']),
        client_opened=False, player_portals_tested=False, portal_entity_roundtrips_tested=True,
        source_log=args.log, test_jar_sha256=sha(args.server/'mods/m2-worldgen-tests-1.0.0.jar'))
    raw['artifacts'] = manifest['artifacts']
    raw['router_validation'] = {key: router.get(key) for key in ['valid', 'indexedResourceCount', 'errors', 'warnings']}
    raw['periodicity_samples'] = [dict(dimension=d, axis=a, width_blocks=int(w), samples=int(n))
        for d, a, w, n in re.findall(r'periodicity_ok level=(\S+) axis=(\w+) width_blocks=(\d+) samples=(\d+)', log)]
    target = ROOT/'docs/worldgen-runtime-validation.json'
    target.write_text(json.dumps(raw, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(target)


if __name__ == '__main__':
    main()
