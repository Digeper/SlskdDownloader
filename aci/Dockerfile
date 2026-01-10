# Custom Docker image for slskd with blobfuse support
FROM slskd/slskd:latest

# Install blobfuse and dependencies
RUN apt-get update && \
    apt-get install -y \
    curl \
    gnupg \
    lsb-release \
    wget \
    fuse3 \
    && rm -rf /var/lib/apt/lists/*

# Install Azure CLI
RUN curl -sL https://aka.ms/InstallAzureCLIDeb | bash

# Install blobfuse2 - Try Debian 11 repository first (compatible with Debian 12)
# If that fails, fall back to direct download from GitHub releases
RUN curl -L https://packages.microsoft.com/config/debian/11/packages-microsoft-prod.deb -o packages-microsoft-prod.deb && \
    dpkg -i packages-microsoft-prod.deb && \
    apt-get update && \
    (apt-get install -y blobfuse2 || \
     (wget -O /tmp/blobfuse2.deb "https://github.com/Azure/azure-storage-fuse/releases/download/blobfuse2-2.3.0/blobfuse2-2.3.0-debian_amd64.deb" && \
      apt-get install -y /tmp/blobfuse2.deb && \
      rm -f /tmp/blobfuse2.deb)) && \
    rm -f packages-microsoft-prod.deb && \
    rm -rf /var/lib/apt/lists/*

# Create mount points
RUN mkdir -p /mnt/blobfuse/downloads \
    /mnt/blobfuse/incomplete \
    /mnt/blobfuse/database \
    /mnt/blobfuse/tmp

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Set working directory
WORKDIR /app

# Use custom entrypoint
ENTRYPOINT ["/entrypoint.sh"]
