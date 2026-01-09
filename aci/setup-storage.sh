#!/bin/bash
# Script to create Azure Storage Account and containers for slskd
# Usage: ./setup-storage.sh <resource-group> <storage-account-name> <location>

set -e

RESOURCE_GROUP=${1:-"muzika-rg"}
STORAGE_ACCOUNT_NAME=${2:-"muzikastorage"}
LOCATION=${3:-"eastus"}

echo "Creating Azure Storage Account: $STORAGE_ACCOUNT_NAME in resource group: $RESOURCE_GROUP"

# Create storage account
az storage account create \
  --resource-group "$RESOURCE_GROUP" \
  --name "$STORAGE_ACCOUNT_NAME" \
  --location "$LOCATION" \
  --sku Standard_LRS \
  --kind StorageV2 \
  --access-tier Hot

# Get storage account key
STORAGE_KEY=$(az storage account keys list \
  --resource-group "$RESOURCE_GROUP" \
  --account-name "$STORAGE_ACCOUNT_NAME" \
  --query "[0].value" -o tsv)

# Create containers
echo "Creating blob containers..."
az storage container create \
  --account-name "$STORAGE_ACCOUNT_NAME" \
  --account-key "$STORAGE_KEY" \
  --name "slskd-downloads" \
  --public-access off

az storage container create \
  --account-name "$STORAGE_ACCOUNT_NAME" \
  --account-key "$STORAGE_KEY" \
  --name "slskd-incomplete" \
  --public-access off

az storage container create \
  --account-name "$STORAGE_ACCOUNT_NAME" \
  --account-key "$STORAGE_KEY" \
  --name "slskd-database" \
  --public-access off

echo "✓ Storage account and containers created successfully"
echo ""
echo "Storage Account Name: $STORAGE_ACCOUNT_NAME"
echo "Storage Account Key: $STORAGE_KEY"
echo ""
echo "IMPORTANT: Store these values in Azure Key Vault:"
echo "  - AZURE_STORAGE_ACCOUNT_NAME = $STORAGE_ACCOUNT_NAME"
echo "  - AZURE_STORAGE_ACCOUNT_KEY = $STORAGE_KEY"
