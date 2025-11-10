#!/usr/bin/env python3
"""
Construye un payload JSON listo para enviar al generador de resumen de proyecto.
Lee `fragments.json`, selecciona fragmentos clave y escribe `prompt_payload.json`.

Uso:
  python3 tools/build_prompt_payload.py [--max N] [--env ENV]

Salida:
  prompt_payload.json
"""
import json
import sys
from pathlib import Path
from argparse import ArgumentParser

ROOT = Path(__file__).resolve().parents[1]
FRAGMENTS = ROOT / 'fragments.json'
OUT = ROOT / 'prompt_payload.json'

parser = ArgumentParser()
parser.add_argument('--max', type=int, default=10, help='Máximo de fragmentos a incluir')
parser.add_argument('--env', type=str, default='local', help='Perfil de despliegue (local/dev/prod)')
args = parser.parse_args()

if not FRAGMENTS.exists():
    print('Error: fragments.json no encontrado. Ejecuta tools/extract_fragments.py primero.')
    sys.exit(2)

with FRAGMENTS.open('r', encoding='utf-8') as f:
    data = json.load(f)

files = data.get('files_scanned', [])
# Priorizar: pom.xml, Dockerfile, docker-compose.yml, application*.properties, src/main
priority_keys = ['pom.xml', 'Dockerfile', 'docker-compose.yml']
selected = []
seen = set()

# 1) pick explicit priority files if present
for key in priority_keys:
    for item in files:
        if item['path'].endswith(key) and item['path'] not in seen:
            selected.append(item)
            seen.add(item['path'])

# 2) pick application*.properties
for item in files:
    if '/application' in item['path'] and item['path'] not in seen:
        selected.append(item)
        seen.add(item['path'])

# 3) pick a few src/ files (tests + main resources)
for item in files:
    if item['path'].startswith('src/') and item['path'] not in seen:
        selected.append(item)
        seen.add(item['path'])
        if len(selected) >= args.max:
            break

# Trim to max
selected = selected[:args.max]

payload = {
    'repo_summary': data.get('repo_summary', '') or "Backend Spring Boot + PostgreSQL (inferred)",
    'files_scanned': selected,
    'env': args.env,
    'focus': ''
}

with OUT.open('w', encoding='utf-8') as f:
    json.dump(payload, f, indent=2, ensure_ascii=False)

print(f'Wrote payload with {len(selected)} fragments to {OUT}')
for it in selected:
    print(' -', it['path'])

