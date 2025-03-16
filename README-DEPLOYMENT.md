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
| `DOMAIN_NAME` | *(Optional)* Your domain name for Nginx configuration (e.g., `example.com`). If not provided, the server's IP address (`ALIYUN_HOST`) will be used instead |

## No Domain Name?

If you don't have a domain name for your server, you can simply omit the `DOMAIN_NAME` secret. The workflow will automatically use your server's IP address (`ALIYUN_HOST` value) in the Nginx configuration. You'll be able to access your application by visiting `http://your-server-ip` in your browser.

## Server Prerequisites

Ensure your Aliyun server has the following:

1. Java 17 installed
2. Nginx installed and running
3. Proper permissions for the deploy directories
4. Sudo access for the deployment user (to configure Nginx)

## Development Environment

The project uses:
- Node.js 20.x
- pnpm 9.x as the package manager
- Java 17 for backend development

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

## Local Development

For local frontend development:

```bash
# Install dependencies
pnpm install

# Start development server
pnpm dev
```

For local backend development:

```bash
# Navigate to backend directory
cd backend

# Run Spring Boot application
./mvnw spring-boot:run
```

## Nginx Configuration

The workflow automatically creates a basic Nginx configuration that:

1. Serves your frontend static files
2. Proxies API requests to your backend application

You can customize the Nginx configuration in the GitHub workflow file if needed.

## Testing Deployment

After setting up the workflow, push changes to your repository and check the GitHub Actions tab to ensure the deployment runs successfully. Then verify your application is accessible via your domain or IP address.

## Troubleshooting

### GitHub Actions Issues

If you encounter issues with the GitHub Actions workflow:

1. **pnpm Installation**: The workflow is set up to install pnpm before using it. If you see errors related to pnpm not being found, ensure the installation step is properly configured and comes before any steps that use pnpm.

2. **Build Errors**: If the frontend build fails, check that your pnpm-lock.yaml file is committed to the repository and that your dependencies are compatible with Node.js 20.

3. **Deployment Errors**: If the deployment to Aliyun fails, verify that all required secrets are correctly set up in your GitHub repository settings and that your server has the necessary permissions and software installed.

4. **Nginx Configuration**: If Nginx fails to restart, check the Nginx error logs on your server (`sudo tail /var/log/nginx/error.log`) to identify any syntax or permission issues. 