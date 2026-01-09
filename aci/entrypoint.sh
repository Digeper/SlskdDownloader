#!/bin/bash
# Entrypoint script to mount Azure Blob Storage and start slskd

set -e

# Configuration from environment variables
STORAGE_ACCOUNT_NAME=${AZURE_STORAGE_ACCOUNT_NAME:-""}
STORAGE_ACCOUNT_KEY=${AZURE_STORAGE_ACCOUNT_KEY:-""}
CONTAINER_DOWNLOADS=${AZURE_STORAGE_CONTAINER_DOWNLOADS:-"slskd-downloads"}
CONTAINER_INCOMPLETE=${AZURE_STORAGE_CONTAINER_INCOMPLETE:-"slskd-incomplete"}
CONTAINER_DATABASE=${AZURE_STORAGE_CONTAINER_DATABASE:-"slskd-database"}

# Mount points
MOUNT_DOWNLOADS="/mnt/blobfuse/downloads"
MOUNT_INCOMPLETE="/mnt/blobfuse/incomplete"
MOUNT_DATABASE="/mnt/blobfuse/database"
TMP_DIR="/mnt/blobfuse/tmp"

# Function to mount blob container
mount_blob() {
    local container_name=$1
    local mount_point=$2
    
    if [ -z "$STORAGE_ACCOUNT_NAME" ] || [ -z "$STORAGE_ACCOUNT_KEY" ]; then
        echo "ERROR: AZURE_STORAGE_ACCOUNT_NAME and AZURE_STORAGE_ACCOUNT_KEY must be set"
        exit 1
    fi
    
    echo "Mounting $container_name to $mount_point..."
    
    # Create connection string file for blobfuse
    echo "accountName $STORAGE_ACCOUNT_NAME" > /tmp/blobfuse_${container_name}.cfg
    echo "accountKey $STORAGE_ACCOUNT_KEY" >> /tmp/blobfuse_${container_name}.cfg
    echo "containerName $container_name" >> /tmp/blobfuse_${container_name}.cfg
    
    # Mount blobfuse
    blobfuse "$mount_point" \
        --tmp-path="${TMP_DIR}/${container_name}" \
        --config-file=/tmp/blobfuse_${container_name}.cfg \
        --file-cache-timeout-in-seconds=120 \
        -o allow_other || {
        echo "ERROR: Failed to mount $container_name"
        exit 1
    }
    
    echo "✓ Successfully mounted $container_name"
}

# Mount all blob containers
echo "Starting blobfuse mounts..."
mount_blob "$CONTAINER_DOWNLOADS" "$MOUNT_DOWNLOADS"
mount_blob "$CONTAINER_INCOMPLETE" "$MOUNT_INCOMPLETE"
mount_blob "$CONTAINER_DATABASE" "$MOUNT_DATABASE"

# Create symlinks to /rw for slskd compatibility
mkdir -p /rw
ln -sf "$MOUNT_DOWNLOADS" /rw/downloads || true
ln -sf "$MOUNT_INCOMPLETE" /rw/incomplete || true
ln -sf "$MOUNT_DATABASE" /rw/database || true

echo "✓ All blob containers mounted successfully"
echo "Starting slskd daemon..."

# Start slskd with original entrypoint
# Pass through all arguments
exec /app/slskd "$@"
