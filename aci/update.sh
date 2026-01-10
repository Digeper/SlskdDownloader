#!/bin/bash
# Simple script to build, push, and redeploy slskd daemon

set -e

# Configuration
ACR_NAME="digeper"
IMAGE_NAME="muzika/slskd-daemon"
FULL_IMAGE="${ACR_NAME}.azurecr.io/${IMAGE_NAME}:latest"
RESOURCE_GROUP="digeper"
CONTAINER_NAME="digeper"
LOCATION="italynorth"

# Get secrets from environment or Azure
AZURE_STORAGE_ACCOUNT_KEY=${AZURE_STORAGE_ACCOUNT_KEY:-""}
ACR_PASSWORD=${ACR_PASSWORD:-""}
SLSKD_SLSK_USERNAME=${SLSKD_SLSK_USERNAME:-"ABCSlime"}
SLSKD_SLSK_PASSWORD=${SLSKD_SLSK_PASSWORD:-""}

if [ -z "$AZURE_STORAGE_ACCOUNT_KEY" ]; then
    echo "Getting Azure Storage Account key..."
    AZURE_STORAGE_ACCOUNT_KEY=$(az storage account keys list \
        --resource-group "$RESOURCE_GROUP" \
        --account-name digeper \
        --query "[0].value" -o tsv)
fi

if [ -z "$ACR_PASSWORD" ]; then
    echo "Getting ACR password..."
    ACR_PASSWORD=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)
fi

if [ -z "$SLSKD_SLSK_PASSWORD" ]; then
    echo "WARNING: SLSKD_SLSK_PASSWORD not set. Using default or you need to set it."
fi

echo "=== Updating slskd daemon ==="
echo ""

# Step 1: Login to ACR
echo "1. Logging into Azure Container Registry..."
az acr login --name "$ACR_NAME" || {
    echo "ERROR: Failed to login to ACR"
    exit 1
}

# Step 2: Build Docker image
echo ""
echo "2. Building Docker image..."
docker build --platform linux/amd64 -t "$FULL_IMAGE" . || {
    echo "ERROR: Docker build failed"
    exit 1
}

# Step 3: Push to ACR
echo ""
echo "3. Pushing image to ACR..."
docker push "$FULL_IMAGE" || {
    echo "ERROR: Failed to push image to ACR"
    exit 1
}

# Step 4: Delete existing container
echo ""
echo "4. Deleting existing container..."
az container delete \
    --resource-group "$RESOURCE_GROUP" \
    --name "$CONTAINER_NAME" \
    --yes 2>/dev/null || echo "Container doesn't exist, continuing..."

sleep 5

# Step 5: Replace placeholders in YAML with actual values
echo ""
echo "5. Preparing deployment YAML with secrets..."
DEPLOY_YAML="/tmp/deploy-${CONTAINER_NAME}-$(date +%s).yaml"

# Find deploy-direct.yaml (check current directory and parent)
if [ -f "deploy-direct.yaml" ]; then
    DEPLOY_TEMPLATE="deploy-direct.yaml"
elif [ -f "../deploy-direct.yaml" ]; then
    DEPLOY_TEMPLATE="../deploy-direct.yaml"
else
    echo "ERROR: deploy-direct.yaml not found"
    exit 1
fi

cp "$DEPLOY_TEMPLATE" "$DEPLOY_YAML"

# Replace placeholders with actual values (handle special chars in sed)
# Escape special characters in values for sed
AZURE_STORAGE_ACCOUNT_KEY_ESC=$(echo "$AZURE_STORAGE_ACCOUNT_KEY" | sed 's/[[\.*^$()+?{|]/\\&/g')
ACR_PASSWORD_ESC=$(echo "$ACR_PASSWORD" | sed 's/[[\.*^$()+?{|]/\\&/g')
SLSKD_SLSK_PASSWORD_ESC=$(echo "$SLSKD_SLSK_PASSWORD" | sed 's/[[\.*^$()+?{|]/\\&/g')

if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS uses BSD sed
    sed -i '' "s|\${AZURE_STORAGE_ACCOUNT_KEY}|${AZURE_STORAGE_ACCOUNT_KEY_ESC}|g" "$DEPLOY_YAML"
    sed -i '' "s|\${ACR_PASSWORD}|${ACR_PASSWORD_ESC}|g" "$DEPLOY_YAML"
    sed -i '' "s|\${SLSKD_SLSK_USERNAME}|${SLSKD_SLSK_USERNAME}|g" "$DEPLOY_YAML"
    sed -i '' "s|\${SLSKD_SLSK_PASSWORD}|${SLSKD_SLSK_PASSWORD_ESC}|g" "$DEPLOY_YAML"
else
    # Linux uses GNU sed
    sed -i "s|\${AZURE_STORAGE_ACCOUNT_KEY}|${AZURE_STORAGE_ACCOUNT_KEY_ESC}|g" "$DEPLOY_YAML"
    sed -i "s|\${ACR_PASSWORD}|${ACR_PASSWORD_ESC}|g" "$DEPLOY_YAML"
    sed -i "s|\${SLSKD_SLSK_USERNAME}|${SLSKD_SLSK_USERNAME}|g" "$DEPLOY_YAML"
    sed -i "s|\${SLSKD_SLSK_PASSWORD}|${SLSKD_SLSK_PASSWORD_ESC}|g" "$DEPLOY_YAML"
fi

# Step 6: Deploy new container
echo ""
echo "6. Deploying new container..."
az container create \
    --resource-group "$RESOURCE_GROUP" \
    --file "$DEPLOY_YAML" || {
    echo "ERROR: Failed to deploy container"
    rm -f "$DEPLOY_YAML"
    exit 1
}

# Cleanup temporary file
rm -f "$DEPLOY_YAML"

echo ""
echo "✓ Update complete!"
echo ""
echo "To check status:"
echo "  az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --query 'instanceView.state' -o tsv"
echo ""
echo "To view logs:"
echo "  az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --tail 50"
echo ""
echo "To follow logs:"
echo "  az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --follow"
