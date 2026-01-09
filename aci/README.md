# slskd Daemon - Azure Container Instances Deployment

This directory contains files for deploying the slskd daemon to Azure Container Instances (ACI) with Azure Blob Storage support via blobfuse.

## Files

- `Dockerfile` - Custom Docker image based on slskd/slskd:latest with blobfuse
- `entrypoint.sh` - Script to mount blob storage and start slskd
- `deploy-aci.sh` - Manual deployment script (for reference)
- `setup-storage.sh` - Script to create Azure Storage Account and containers

## Prerequisites

1. Azure Storage Account with containers:
   - `slskd-downloads`
   - `slskd-incomplete`
   - `slskd-database`

2. Azure Container Registry (ACR)

3. Required GitHub Secrets:
   - `ACR_NAME` - Azure Container Registry name
   - `ACR_USERNAME` - ACR username
   - `ACR_PASSWORD` - ACR password
   - `AZURE_CREDENTIALS` - Azure service principal credentials (JSON)
   - `AZURE_STORAGE_ACCOUNT_NAME` - Storage account name
   - `AZURE_STORAGE_ACCOUNT_KEY` - Storage account key
   - `SLSKD_SLSK_USERNAME` - Soulseek username
   - `SLSKD_SLSK_PASSWORD` - Soulseek password
   - `ACI_RESOURCE_GROUP` - Resource group for ACI deployment
   - `ACI_LOCATION` - Azure region for ACI

## Deployment

Deployment is automated via GitHub Actions workflow: `.github/workflows/slskd-daemon-deploy.yml`

The workflow will:
1. Build the custom Docker image with blobfuse
2. Push to Azure Container Registry
3. Deploy to Azure Container Instances with public IP
4. Expose ports: 5030 (API), 5031, 50300 (P2P)

## Manual Setup (if needed)

### 1. Create Storage Account

```bash
./setup-storage.sh <resource-group> <storage-account-name> <location>
```

### 2. Build and Push Docker Image

```bash
docker build -t ${ACR_NAME}.azurecr.io/muzika/slskd-daemon:latest .
docker push ${ACR_NAME}.azurecr.io/muzika/slskd-daemon:latest
```

### 3. Deploy to ACI

```bash
./deploy-aci.sh <resource-group> <container-group-name> <image-name>
```

## Configuration

After deployment, the ACI instance will have a public IP. Update the ConfigMap in `SlskdDownload/k8s/configmap.yaml` with:

```yaml
SLSKD_API_URL: "http://<PUBLIC_IP>:5030"
```

## Ports

- **5030** - slskd API (HTTP)
- **5031** - slskd additional port
- **50300** - P2P connections (must be publicly accessible)

## Storage

Blob storage is mounted via blobfuse at:
- `/rw/downloads` → `slskd-downloads` container
- `/rw/incomplete` → `slskd-incomplete` container
- `/rw/database` → `slskd-database` container
