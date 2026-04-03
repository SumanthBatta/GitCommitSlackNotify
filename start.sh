#!/bin/bash

# ============================================
# Tunnel + GitHub Webhook Updater
# (Run Spring Boot app manually from IntelliJ first!)
# ============================================

ENV_FILE="$(dirname "$0")/.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env file not found!"
    exit 1
fi
source "$ENV_FILE"

APP_PORT=8080

# Step 1: Check if app is running
if ! curl -s -o /dev/null -w "%{http_code}" http://localhost:$APP_PORT/h2-console 2>/dev/null | grep -q "302"; then
    echo "❌ App is not running on port $APP_PORT. Start it from IntelliJ first!"
    exit 1
fi
echo "✅ App is running on port $APP_PORT"

# Step 2: Start tunnel
echo "🌐 Starting tunnel..."
ssh -R 80:localhost:$APP_PORT nokey@localhost.run > /tmp/tunnel.log 2>&1 &
TUNNEL_PID=$!

echo "⏳ Waiting for tunnel URL..."
TUNNEL_URL=""
for i in {1..20}; do
    TUNNEL_URL=$(grep -o 'https://[a-z0-9]*\.lhr\.life' /tmp/tunnel.log | head -1)
    if [ -n "$TUNNEL_URL" ]; then
        echo "✅ Tunnel URL: $TUNNEL_URL"
        break
    fi
    sleep 2
done

if [ -z "$TUNNEL_URL" ]; then
    echo "❌ Failed to get tunnel URL. Check /tmp/tunnel.log"
    exit 1
fi

# Step 3: Update GitHub webhook
echo "🔗 Updating GitHub webhook..."
RESPONSE=$(curl -s -X PATCH \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"config\":{\"url\":\"$TUNNEL_URL/api/webhook/github\",\"content_type\":\"json\",\"insecure_ssl\":\"0\"},\"events\":[\"push\"],\"active\":true}" \
    "https://api.github.com/repos/$GITHUB_REPO/hooks/$WEBHOOK_ID")

UPDATED_URL=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['config']['url'])" 2>/dev/null)

if [ -n "$UPDATED_URL" ]; then
    echo "✅ GitHub webhook updated to: $UPDATED_URL"
else
    echo "❌ Failed to update webhook. Response: $RESPONSE"
    kill $TUNNEL_PID 2>/dev/null
    exit 1
fi

echo ""
echo "============================================"
echo "🎉 Tunnel is running!"
echo "============================================"
echo "🌐 Tunnel:   $TUNNEL_URL"
echo "🔗 Webhook:  $TUNNEL_URL/api/webhook/github"
echo "============================================"
echo "Now push code to GitHub and check Slack!"
echo "Press Ctrl+C to stop tunnel."
echo ""

cleanup() {
    echo ""
    echo "🛑 Stopping tunnel..."
    kill $TUNNEL_PID 2>/dev/null
    echo "✅ Stopped."
}
trap cleanup EXIT
tail -f /tmp/tunnel.log
