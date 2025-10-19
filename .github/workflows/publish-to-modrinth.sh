#!/usr/bin/env bash
set -euo pipefail

# === ⚙️ Config ===
MODRINTH_TOKEN="${MODRINTH_TOKEN:-}"
PROJECT_ID="${PROJECT_ID:-}"
DIST_DIR="${DIST_DIR:-dist}"
TITLE="${TITLE:-}"
CHANGELOG="${CHANGELOG:-}"
TYPE="${TYPE:-alpha}" # release | beta | alpha

# === 🔍 Validation ===
if [ -z "$MODRINTH_TOKEN" ]; then
  echo "❌ Missing MODRINTH_TOKEN environment variable."
  exit 1
fi
if [ -z "$PROJECT_ID" ]; then
  echo "❌ Missing PROJECT_ID environment variable."
  exit 1
fi

FILE=$(ls "$DIST_DIR"/*.jar 2>/dev/null | head -n 1 || true)
if [ -z "$FILE" ]; then
  echo "❌ Could not find any .jar file in '$DIST_DIR/'."
  exit 1
fi
FILE_NAME=$(basename "$FILE")

# === 🧮 Version extraction ===
VERSION=$(echo "$FILE_NAME" | sed -E 's/^[^-]+-(.+)\.jar$/\1/')
DATE=$(date -u +"%Y-%m-%d %H:%M UTC")

# === 🧾 Metadata generation ===
if [ -z "$TITLE" ]; then
  TITLE="Automated Build $VERSION"
fi
if [ -z "$CHANGELOG" ]; then
  CHANGELOG="Build generated automatically from commit ${GITHUB_SHA:-unknown} on $DATE."
fi

echo "🧾 Preparing Modrinth metadata..."
echo "  File:     $FILE_NAME"
echo "  Version:  $VERSION"
echo "  Type:     $TYPE"
echo "  Title:    $TITLE"

# === 🧱 Create metadata.json ===
cat > metadata.json <<EOF
{
  "name": "$TITLE",
  "version_number": "$VERSION",
  "changelog": "$CHANGELOG",
  "version_type": "$TYPE",
  "loaders": ["paper"],
  "game_versions": ["1.20","1.20.1","1.20.2","1.20.3","1.20.4","1.20.5","1.20.6","1.21","1.21.1","1.21.2","1.21.3","1.21.4","1.21.5","1.21.6","1.21.7","1.21.8"],
  "project_id": "$PROJECT_ID",
  "featured": false,
  "status": "listed",
  "file_parts": ["file"],
  "primary_file": "file",
  "dependencies": []
}
EOF

# === ☁️ Upload ===
echo "🚀 Uploading to Modrinth..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o response.json \
  -X POST "https://api.modrinth.com/v2/version" \
  -H "Authorization: Bearer $MODRINTH_TOKEN" \
  -F "data=@metadata.json;type=application/json" \
  -F "file=@$FILE;type=application/java-archive")

if [[ "$HTTP_CODE" -lt 200 || "$HTTP_CODE" -ge 300 ]]; then
  echo "❌ Upload failed (HTTP $HTTP_CODE)"
  cat response.json
  exit 1
fi

echo "✅ Upload complete!"
