# Custom Docker image for slskd with blobfuse support
FROM slskd/slskd:latest

# Install blobfuse and dependencies
RUN apt-get update && \
    apt-get install -y \
    curl \
    gnupg \
    lsb-release \
    fuse \
    && rm -rf /var/lib/apt/lists/*

# Install Azure CLI
RUN curl -sL https://aka.ms/InstallAzureCLIDeb | bash

# Install blobfuse
RUN curl -L https://packages.microsoft.com/config/debian/11/packages-microsoft-prod.deb -o packages-microsoft-prod.deb && \
    dpkg -i packages-microsoft-prod.deb && \
    apt-get update && \
    apt-get install -y blobfuse && \
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
