#!/bin/bash
# Script to deploy slskd to Azure Container Instances
# Usage: ./deploy-aci.sh <resource-group> <container-group-name> <image-name> <public-ip>

set -e

RESOURCE_GROUP=${1:-"muzika-rg"}
CONTAINER_GROUP_NAME=${2:-"slskd-daemon"}
IMAGE_NAME=${3:-"${ACR_NAME}.azurecr.io/muzika/slskd-daemon:latest"}
PUBLIC_IP=${4:-""}

# Get secrets from environment or Azure Key Vault
STORAGE_ACCOUNT_NAME=${AZURE_STORAGE_ACCOUNT_NAME:-""}
STORAGE_ACCOUNT_KEY=${AZURE_STORAGE_ACCOUNT_KEY:-""}
SLSKD_USERNAME=${SLSKD_SLSK_USERNAME:-""}
SLSKD_PASSWORD=${SLSKD_SLSK_PASSWORD:-""}

if [ -z "$STORAGE_ACCOUNT_NAME" ] || [ -z "$STORAGE_ACCOUNT_KEY" ]; then
    echo "ERROR: AZURE_STORAGE_ACCOUNT_NAME and AZURE_STORAGE_ACCOUNT_KEY must be set"
    exit 1
fi

if [ -z "$SLSKD_USERNAME" ] || [ -z "$SLSKD_PASSWORD" ]; then
    echo "ERROR: SLSKD_SLSK_USERNAME and SLSKD_SLSK_PASSWORD must be set"
    exit 1
fi

echo "Deploying slskd to Azure Container Instances..."
echo "Resource Group: $RESOURCE_GROUP"
echo "Container Group: $CONTAINER_GROUP_NAME"
echo "Image: $IMAGE_NAME"

# Delete existing container group if it exists
az container delete \
  --resource-group "$RESOURCE_GROUP" \
  --name "$CONTAINER_GROUP_NAME" \
  --yes 2>/dev/null || true

# Wait a bit for deletion to complete
sleep 5

# Create container group with public IP
az container create \
  --resource-group "$RESOURCE_GROUP" \
  --name "$CONTAINER_GROUP_NAME" \
  --image "$IMAGE_NAME" \
  --cpu 2 \
  --memory 4 \
  --registry-login-server "${ACR_NAME}.azurecr.io" \
  --registry-username "${ACR_USERNAME}" \
  --registry-password "${ACR_PASSWORD}" \
  --ip-address Public \
  --ports 5030 5031 50300 \
  --protocol TCP \
  --environment-variables \
    APP_DIR=/app \
    SLSKD_DOWNLOADS_DIR=/rw/downloads \
    SLSKD_INCOMPLETE_DIR=/rw/incomplete \
    SLSKD_REMOTE_CONFIGURATION=true \
    SLSKD_SLSK_USERNAME="$SLSKD_USERNAME" \
    SLSKD_SLSK_PASSWORD="$SLSKD_PASSWORD" \
    AZURE_STORAGE_ACCOUNT_NAME="$STORAGE_ACCOUNT_NAME" \
    AZURE_STORAGE_ACCOUNT_KEY="$STORAGE_ACCOUNT_KEY" \
    AZURE_STORAGE_CONTAINER_DOWNLOADS=slskd-downloads \
    AZURE_STORAGE_CONTAINER_INCOMPLETE=slskd-incomplete \
    AZURE_STORAGE_CONTAINER_DATABASE=slskd-database \
  --restart-policy Always

# Get the public IP
PUBLIC_IP=$(az container show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$CONTAINER_GROUP_NAME" \
  --query "ipAddress.ip" -o tsv)

echo ""
echo "✓ slskd deployed successfully!"
echo "Public IP: $PUBLIC_IP"
echo "API URL: http://$PUBLIC_IP:5030"
echo ""
echo "Update your ConfigMap with: SLSKD_API_URL=http://$PUBLIC_IP:5030"
