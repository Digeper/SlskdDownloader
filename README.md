# SlskdDownload

## What it does

Handles downloads from slskd (Soulseek daemon). Manages search operations and coordinates with the slskd API to download music files. Processes download requests via Kafka.

## Local Setup

1. Ensure slskd is running and accessible (default: `http://localhost:5030`)
2. Update `slskd.api.url` in `application.properties` if needed
3. Ensure Kafka is running on `localhost:9092`
4. Run: `mvn spring-boot:run`
5. Service starts on port `8080`

## Deployment

Deploy to Kubernetes namespace `muzika`:
```bash
kubectl apply -k k8s/
```

Image: `${ACR_NAME}.azurecr.io/muzika/slskd-download:latest`

Requires: slskd API access, Kafka cluster, ConfigMap
