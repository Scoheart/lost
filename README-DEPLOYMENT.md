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

3. **Static Asset Issues**: If you encounter errors like `Could not resolve "../assets/images/404.svg"`, ensure that all referenced assets exist in the correct paths. The project structure expects images to be in `frontend/src/assets/images/`.

4. **Deployment Errors**: If the deployment to Aliyun fails, verify that all required secrets are correctly set up in your GitHub repository settings and that your server has the necessary permissions and software installed.

5. **Nginx Configuration**: If Nginx fails to restart, check the Nginx error logs on your server (`sudo tail /var/log/nginx/error.log`) to identify any syntax or permission issues.

### Backend Application Issues

If the backend application fails to start on deployment:

1. **Database Connection**: Check the MySQL connection settings in `application-prod.yml`. Ensure MySQL is running on the server and the database exists with the correct credentials.

2. **Redis Connection**: Ensure Redis is running on the server or update the Redis configuration to match your server setup.

3. **Permissions**: Ensure that the deployment directory and all subdirectories have the correct permissions:
   ```bash
   chmod -R 755 /path/to/deployment
   chmod -R 777 /path/to/deployment/uploads # If needed for file uploads
   ```

4. **Memory Issues**: If the JVM is running out of memory, adjust the JVM options in `deploy.sh`:
   ```
   JVM_OPTS="-Xms256m -Xmx512m" # Reduce memory if server has limited resources
   ```

5. **Check Logs**: The most valuable information will be in the application logs:
   ```bash
   sudo tail -f /path/to/deployment/logs/app.log
   ```

6. **Database Schema**: If the database schema is inconsistent, you might need to run schema migration scripts manually.

7. **Network Issues**: Ensure that the ports required by the application (8080) are open in the firewall:
   ```bash
   sudo ufw allow 8080/tcp # If using UFW firewall
   ```

8. **Profile Issues**: Ensure that the `spring.profiles.active=prod` property is correctly set in the deployment command.

### Static Assets Best Practices

1. **Asset Organization**: Keep all static assets in the `frontend/src/assets/` directory, organized by type (images, icons, etc.)

2. **Import Paths**: When importing assets in Vue components, use relative paths from the component file (e.g., `../assets/images/logo.svg`) or alias imports (e.g., `@/assets/images/logo.svg`).

3. **SVG Usage**: For critical UI elements, consider using SVG files that can be embedded directly in components or using icon libraries like Element Plus icons.

4. **Asset Optimization**: Large images should be optimized before adding them to the project. Consider using tools like ImageOptim or TinyPNG. 