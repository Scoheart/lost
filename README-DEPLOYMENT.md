# Deployment Guide

## GitHub Secrets Configuration

To deploy both the backend and frontend applications to Aliyun ECS, you need to configure the following GitHub secrets:

| Secret Name | Description |
|-------------|-------------|
| `ALIYUN_SSH_PRIVATE_KEY` | SSH private key for connecting to your Aliyun server |
| `ALIYUN_USER` | SSH username for your Aliyun server (e.g., `root`) |
| `ALIYUN_HOST` | IP address or hostname of your Aliyun server |
| `DEPLOY_PATH` | Path where backend application will be deployed (e.g., `/opt/lostandfound/backend`) |
| `FRONTEND_DEPLOY_PATH` | Path where frontend files will be deployed (e.g., `/var/www/html/lostandfound`) |
| `DOMAIN_NAME` | Your domain name for Nginx configuration (e.g., `example.com`) |

## Server Prerequisites

Ensure your Aliyun server has the following:

1. Java 17 installed
2. Nginx installed and running
3. Proper permissions for the deploy directories
4. Sudo access for the deployment user (to configure Nginx)

## Backend Configuration

The backend deployment script (`deploy.sh`) should contain:

```bash
#!/bin/bash
# Stop existing application
pid=$(pgrep -f "java -jar app.jar" || echo "")
if [ ! -z "$pid" ]; then
  echo "Stopping existing application (PID: $pid)..."
  kill $pid
  sleep 5
fi

# Start new application
echo "Starting application..."
nohup java -jar app.jar > app.log 2>&1 &
echo "Application started."
```

Make sure to create this file in your backend directory.

## Frontend Environment Variables

For proper frontend deployment, ensure your `.env.production` file contains the correct API base URL. If your API is served from the same domain but under the `/api` path, use:

```
VITE_API_BASE_URL="/api"
```

If your API is hosted on a different domain, update accordingly.

## Nginx Configuration

The workflow automatically creates a basic Nginx configuration that:

1. Serves your frontend static files
2. Proxies API requests to your backend application

You can customize the Nginx configuration in the GitHub workflow file if needed.

## Testing Deployment

After setting up the workflow, push changes to your repository and check the GitHub Actions tab to ensure the deployment runs successfully. Then verify your application is accessible via your domain. 