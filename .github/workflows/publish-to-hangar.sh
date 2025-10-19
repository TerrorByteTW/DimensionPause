#!/usr/bin/env bash
set -euo pipefail

# === ⚙️ Config ===
PROJECT_SLUG="${PROJECT_SLUG:-}"
HANGAR_TOKEN="${HANGAR_TOKEN:-}"
CHANNEL="${CHANNEL:-Snapshot}"   # Release | Snapshot | Beta
TITLE="${TITLE:-}"
DESCRIPTION="${DESCRIPTION:-}"
DIST_DIR="${DIST_DIR:-dist}"

# === 🔍 Validation ===
if [ -z "$HANGAR_TOKEN" ]; then
  echo "❌ Missing HANGAR_TOKEN environment variable."
  exit 1
fi
if [ -z "$PROJECT_SLUG" ]; then
  echo "❌ Missing PROJECT_SLUG environment variable."
  exit 1
fi

FILE=$(ls "$DIST_DIR"/*.jar 2>/dev/null | head -n 1 || true)
if [ -z "$FILE" ]; then
  echo "❌ Could not find .jar file in '$DIST_DIR/'."
  exit 1
fi

# === 🧮 Version extraction ===
BASENAME=$(basename "$FILE")
VERSION=$(echo "$BASENAME" | sed -E 's/^[^-]+-(.+)\.jar$/\1/')
DATE=$(date -u +"%Y-%m-%d %H:%M UTC")

if [ -z "$TITLE" ]; then
  TITLE="Automated Upload $VERSION"
fi
if [ -z "$DESCRIPTION" ]; then
  DESCRIPTION="Automated upload from commit ${GITHUB_SHA:-unknown} on $DATE."
fi

echo "📦 Preparing Hangar upload..."
echo "  Project : $PROJECT_SLUG"
echo "  Version : $VERSION"
echo "  Channel : $CHANNEL"
echo "  File    : $FILE"

# === 🔑 Authenticate ===
AUTH_RESPONSE=$(curl -sfS -X POST "https://hangar.papermc.io/api/v1/authenticate?apiKey=$HANGAR_TOKEN")
TOKEN=$(echo "$AUTH_RESPONSE" | jq -r '.token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "❌ Failed to obtain JWT token from Hangar."
  echo "$AUTH_RESPONSE"
  exit 1
fi

# === 🧱 Version metadata ===
cat > versionUpload.json <<EOF
{
  "version": "$VERSION",
  "channel": "$CHANNEL",
  "description": "$DESCRIPTION",
  "platformDependencies": {
    "PAPER": ["1.20","1.20.1","1.20.2","1.20.3","1.20.4","1.20.5","1.20.6","1.21","1.21.1","1.21.2","1.21.3","1.21.4","1.21.5","1.21.6","1.21.7","1.21.8"]
  },
  "pluginDependencies": {},
  "files": [
    {
      "platforms": [
        "PAPER"
      ]
    }
  ]
}
EOF

# === ☁️ Upload ===
echo "🚀 Uploading to Hangar..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o response.json \
  -X POST "https://hangar.papermc.io/api/v1/projects/$PROJECT_SLUG/upload" \
  -H "Authorization: HangarAuth $TOKEN" \
  -H "accept: application/json" \
  -F "files=@$FILE;type=application/java-archive" \
  -F "versionUpload=@versionUpload.json;type=application/json")

if [[ "$HTTP_CODE" -lt 200 || "$HTTP_CODE" -ge 300 ]]; then
  echo "❌ Upload failed (HTTP $HTTP_CODE)"
  cat response.json
  exit 1
fi

URL=$(jq -r '.url' response.json 2>/dev/null || echo "unknown")
echo "✅ Upload complete!"
echo "🌐 Version URL: ${URL}"
