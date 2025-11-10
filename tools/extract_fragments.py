#!/usr/bin/env python3
"""
Pequeño script para extraer fragmentos relevantes del repo y generar un JSON
compatible con la plantilla `project_summary_backend_generator.md`.
Genera `fragments.json` en la carpeta raíz del repo.

Uso:
  python3 tools/extract_fragments.py

Selecciona archivos: pom.xml, Dockerfile, docker-compose.yml, application*.properties,
archivos bajo src/, y cualquier archivo named README or *.md de la raíz.
"""
import os
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "fragments.json"

# Patterns / filenames to capture
target_names = {"pom.xml", "Dockerfile", "docker-compose.yml"}
property_glob = "**/application*.properties"
other_globs = ["src/**", "**/Dockerfile", "**/pom.xml", "**/docker-compose.yml", "**/*.yml", "**/*.yaml"]

MAX_CHARS = 2000

files_scanned = []

def add_file(path: Path):
    try:
        text = path.read_text(encoding="utf-8", errors="replace")
    except Exception:
        return
    excerpt = text[:MAX_CHARS]
    files_scanned.append({
        "path": str(path.relative_to(ROOT)),
        "excerpt": excerpt
    })

# 1) explicit filenames in repo root
for name in target_names:
    p = ROOT / name
    if p.exists():
        add_file(p)

# 2) application*.properties anywhere
for p in ROOT.glob(property_glob):
    if p.is_file():
        add_file(p)

# 3) scan common patterns but avoid huge binary files
for dirpath, dirnames, filenames in os.walk(ROOT):
    # skip target/ and .git and node_modules for speed
    if any(skip in dirpath for skip in ("/target", "/.git", "/node_modules", "/.venv")):
        continue
    for fname in filenames:
        if fname.endswith(('.java', '.xml', '.yml', '.yaml', '.properties', '.md')) or fname in target_names:
            p = Path(dirpath) / fname
            if p.is_file():
                # avoid too large files
                try:
                    if p.stat().st_size > 200_000:  # 200KB
                        continue
                except Exception:
                    pass
                add_file(p)

payload = {
    "repo_summary": "",
    "files_scanned": files_scanned,
    "env": "local",
    "focus": "",
}

with OUT.open("w", encoding="utf-8") as f:
    json.dump(payload, f, indent=2, ensure_ascii=False)

print(f"Wrote {len(files_scanned)} fragments to {OUT}")
print("Example paths:")
for item in files_scanned[:10]:
    print(" -", item['path'])

print("Done.")

