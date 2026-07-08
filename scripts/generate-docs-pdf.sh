#!/usr/bin/env bash
# Assemble la documentation Markdown et génère HTML + PDF (si possible).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DOCS="$ROOT/docs"
OUT_DIR="$DOCS/pdf"
MERGED="$OUT_DIR/Kids-Activities-MVP-Guide-complet.md"
HTML="$OUT_DIR/Kids-Activities-MVP-Guide-complet.html"
PDF="$OUT_DIR/Kids-Activities-MVP-Guide-complet.pdf"

mkdir -p "$OUT_DIR"

echo "→ Assemblage des chapitres Markdown..."
{
  echo "# Kids Activities MVP — Guide complet"
  echo ""
  echo "*Généré le $(date '+%d/%m/%Y à %H:%M')*"
  echo ""
  echo "---"
  echo ""

  for file in \
    "$DOCS/01-demarrage-rapide.md" \
    "$DOCS/02-architecture.md" \
    "$DOCS/03-user-stories.md" \
    "$DOCS/04-plan-de-test.md" \
    "$DOCS/05-comptes-et-configuration.md" \
    "$DOCS/06-api-et-postman.md"
  do
    if [[ -f "$file" ]]; then
      tail -n +2 "$file"
      echo ""
      echo "---"
      echo ""
    fi
  done
} > "$MERGED"

echo "→ Markdown : $MERGED"

# --- HTML (toujours généré, imprimable en PDF depuis le navigateur) ---
if command -v npx >/dev/null 2>&1; then
  echo "→ Génération HTML..."
  npx --yes marked-cli "$MERGED" -o "$HTML" 2>/dev/null || \
  npx --yes marked "$MERGED" > "${HTML}.body" 2>/dev/null && {
    cat > "$HTML" <<'HTMLEOF'
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <title>Kids Activities MVP — Guide complet</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 900px; margin: 2rem auto; padding: 0 1.5rem; line-height: 1.6; color: #222; }
    h1,h2,h3 { color: #1a365d; }
    table { border-collapse: collapse; width: 100%; margin: 1rem 0; font-size: 0.9rem; }
    th, td { border: 1px solid #ccc; padding: 0.4rem 0.6rem; text-align: left; }
    th { background: #f0f4f8; }
    code, pre { background: #f5f5f5; border-radius: 4px; }
    pre { padding: 1rem; overflow-x: auto; }
    hr { margin: 2rem 0; border: none; border-top: 1px solid #ddd; }
    @media print { body { max-width: none; } }
  </style>
</head>
<body>
HTMLEOF
    cat "${HTML}.body" >> "$HTML"
    echo "</body></html>" >> "$HTML"
    rm -f "${HTML}.body"
  }
fi

# Fallback HTML minimal si marked échoue
if [[ ! -f "$HTML" ]]; then
  python3 - "$MERGED" "$HTML" <<'PYEOF'
import sys, html, re
src, dst = sys.argv[1], sys.argv[2]
text = open(src, encoding="utf-8").read()
lines = []
for line in text.splitlines():
    if line.startswith("# "):
        lines.append(f"<h1>{html.escape(line[2:])}</h1>")
    elif line.startswith("## "):
        lines.append(f"<h2>{html.escape(line[3:])}</h2>")
    elif line.startswith("### "):
        lines.append(f"<h3>{html.escape(line[4:])}</h3>")
    elif line.startswith("- "):
        lines.append(f"<li>{html.escape(line[2:])}</li>")
    elif line.startswith("```"):
        continue
    elif line.strip() == "---":
        lines.append("<hr>")
    elif line.strip():
        lines.append(f"<p>{html.escape(line)}</p>")
body = "\n".join(lines)
open(dst, "w", encoding="utf-8").write(f"""<!DOCTYPE html><html lang="fr"><head><meta charset="utf-8"><title>Guide MVP</title>
<style>body{{font-family:system-ui,sans-serif;max-width:900px;margin:2rem auto;padding:0 1.5rem;line-height:1.6}}</style></head><body>{body}</body></html>""")
PYEOF
fi

echo "→ HTML : $HTML"
echo "  Pour PDF : ouvrir dans le navigateur → Fichier → Imprimer → Enregistrer en PDF"

# --- PDF via Pandoc ou md-to-pdf ancienne version ---
if command -v pandoc >/dev/null 2>&1; then
  echo "→ Génération PDF avec Pandoc..."
  pandoc "$MERGED" -o "$PDF" -V geometry:margin=2cm -V lang=fr --toc --toc-depth=2
  echo "✓ PDF : $PDF"
elif command -v npx >/dev/null 2>&1; then
  echo "→ Tentative PDF (md-to-pdf@5.2.4)..."
  if npx --yes md-to-pdf@5.2.4 "$MERGED" --dest "$PDF" 2>/dev/null; then
    echo "✓ PDF : $PDF"
  else
    echo "⚠ PDF non généré automatiquement — utilisez le HTML ci-dessus (Imprimer → PDF)"
  fi
fi

echo ""
echo "Documentation disponible :"
echo "  • Markdown chapitres : $DOCS/"
echo "  • Guide complet MD  : $MERGED"
echo "  • Guide complet HTML: $HTML"
