# 🐳 Docker Deployment Guide - RIT Placement Portal

## 📋 Overview

This guide explains how to run the RIT Placement Portal using Docker and Docker Compose.

---

## 🎯 Architecture

```
┌─────────────────────────────────────────┐
│         Docker Compose Stack            │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐    ┌──────────────┐  │
│  │   Tomcat     │    │    MySQL     │  │
│  │   (App)      │◄───┤    (DB)      │  │
│  │  Port 8080   │    │  Port 3306   │  │
│  └──────────────┘    └──────────────┘  │
│         │                    │          │
│         │                    │          │
│    WAR Deploy          Schema Init      │
│                                         │
└─────────────────────────────────────────┘
         │                    │
         ▼                    ▼
   http://localhost:8080   mysql://localhost:3306
```

---

## 📦 Prerequisites

### Required Software
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Maven**: Version 3.6+ (for local builds)
- **Java**: JDK 17 (for local development)

### Installation

#### Windows
```powershell
# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop

# Verify installation
docker --version
docker compose version
```

#### Linux
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt-get install docker-compose-plugin

# Verify installation
docker --version
docker compose version
```

#### macOS
```bash
# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop

# Verify installation
docker --version
docker compose version
```

---

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd rit-placement-portal
```

### 2. Build and Run
```bash
# Build and start all services
docker compose up --build

# Or run in detached mode (background)
docker compose up --build -d
```

### 3. Access Application
- **Application**: http://localhost:8080
- **Database**: localhost:3306

### 4. Default Credentials
- **Username**: ADMIN001
- **Password**: admin123

---

## 📁 Project Structure

```
rit-placement-portal/
├── Dockerfile                  # Application container definition
├── docker-compose.yml          # Multi-container orchestration
├── .dockerignore              # Files to exclude from build
├── .env.example               # Environment variables template
├── pom.xml                    # Maven build configuration
├── src/                       # Java source code
│   └── main/
│       ├── java/
│       └── webapp/
├── sql/
│   └── docker-init.sql        # Database initialization script
└── data/                      # Persistent data (uploads, etc.)
```

---

## ⚙️ Configuration

### Environment Variables

Create a `.env` file from the template:

```bash
cp .env.example .env
```

Edit `.env` with your values:

```env
# Database Configuration
DB_HOST=db
DB_PORT=3306
DB_NAME=placement_system
DB_USER=root
DB_PASSWORD=placement_root_2024

# SMTP Configuration (optional)
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM_EMAIL=noreply@ritplacement.edu
```

### Database Configuration

The database is automatically initialized with:
- All required tables
- Indexes for performance
- Default admin user

**Database Details:**
- **Host**: db (internal) / localhost (external)
- **Port**: 3306
- **Database**: placement_system
- **User**: root
- **Password**: placement_root_2024

---

## 🔧 Docker Commands

### Start Services
```bash
# Start all services
docker compose up

# Start in background
docker compose up -d

# Rebuild and start
docker compose up --build
```

### Stop Services
```bash
# Stop all services
docker compose down

# Stop and remove volumes (⚠️ deletes data)
docker compose down -v
```

### View Logs
```bash
# View all logs
docker compose logs

# View app logs
docker compose logs app

# View database logs
docker compose logs db

# Follow logs (real-time)
docker compose logs -f app
```

### Restart Services
```bash
# Restart all services
docker compose restart

# Restart specific service
docker compose restart app
```

### Execute Commands
```bash
# Access app container shell
docker compose exec app bash

# Access database shell
docker compose exec db mysql -u root -p

# Run Maven commands
docker compose exec app mvn clean package
```

---

## 🗄️ Database Management

### Access MySQL
```bash
# Using Docker
docker compose exec db mysql -u root -p
# Password: placement_root_2024

# Using MySQL client (from host)
mysql -h localhost -P 3306 -u root -p
```

### Backup Database
```bash
# Backup to file
docker compose exec db mysqldump -u root -p placement_system > backup.sql

# Restore from file
docker compose exec -T db mysql -u root -p placement_system < backup.sql
```

### Reset Database
```bash
# Stop services
docker compose down

# Remove database volume
docker volume rm rit-placement-portal_mysql_data

# Restart (will reinitialize)
docker compose up -d
```

---

## 🔍 Troubleshooting

### Issue: Port Already in Use

**Error**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution**:
```bash
# Find process using port
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080

# Kill process or change port in docker-compose.yml
ports:
  - "8081:8080"  # Use different host port
```

### Issue: Database Connection Failed

**Error**: `Communications link failure`

**Solution**:
```bash
# Check database is running
docker compose ps

# Check database logs
docker compose logs db

# Verify database is healthy
docker compose exec db mysqladmin ping -u root -p

# Restart database
docker compose restart db
```

### Issue: Application Not Starting

**Error**: `Application failed to start`

**Solution**:
```bash
# Check application logs
docker compose logs app

# Verify WAR file exists
docker compose exec app ls -la /usr/local/tomcat/webapps/

# Rebuild application
docker compose up --build

# Check Tomcat logs
docker compose exec app tail -f /usr/local/tomcat/logs/catalina.out
```

### Issue: Out of Memory

**Error**: `java.lang.OutOfMemoryError`

**Solution**:
```yaml
# Edit docker-compose.yml
environment:
  CATALINA_OPTS: "-Xms1024m -Xmx2048m"  # Increase memory
```

### Issue: Database Not Initialized

**Error**: `Table 'placement_system.users' doesn't exist`

**Solution**:
```bash
# Remove and recreate database
docker compose down -v
docker compose up -d

# Or manually initialize
docker compose exec db mysql -u root -p placement_system < sql/docker-init.sql
```

---

## 🧪 Testing

### Health Checks

```bash
# Check application health
curl http://localhost:8080/

# Check database health
docker compose exec db mysqladmin ping -u root -p

# Check all services status
docker compose ps
```

### Verify Database

```bash
# Connect to database
docker compose exec db mysql -u root -p placement_system

# Run queries
mysql> SHOW TABLES;
mysql> SELECT COUNT(*) FROM users;
mysql> SELECT * FROM users WHERE role='ADMIN';
```

### Test Application

1. **Access Application**: http://localhost:8080
2. **Login**: ADMIN001 / admin123
3. **Verify Features**:
   - Dashboard loads
   - Database queries work
   - Navigation functional

---

## 📊 Monitoring

### Container Stats
```bash
# View resource usage
docker stats

# View specific container
docker stats placement-app
```

### Logs
```bash
# Application logs
docker compose logs -f app

# Database logs
docker compose logs -f db

# Last 100 lines
docker compose logs --tail=100 app
```

### Health Status
```bash
# Check health status
docker compose ps

# Detailed inspection
docker inspect placement-app
```

---

## 🔒 Security

### Production Recommendations

1. **Change Default Passwords**
```yaml
environment:
  MYSQL_ROOT_PASSWORD: <strong-password>
  DB_PASSWORD: <strong-password>
```

2. **Use Secrets Management**
```bash
# Use Docker secrets instead of environment variables
docker secret create db_password password.txt
```

3. **Limit Port Exposure**
```yaml
# Don't expose database port in production
# ports:
#   - "3306:3306"  # Comment out
```

4. **Enable SSL/TLS**
```yaml
# Add SSL certificates
volumes:
  - ./certs:/certs:ro
environment:
  CATALINA_OPTS: "-Djavax.net.ssl.keyStore=/certs/keystore.jks"
```

5. **Use Non-Root User**
```dockerfile
# Add to Dockerfile
USER tomcat
```

---

## 🚀 Production Deployment

### Using Docker Swarm

```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml placement

# Scale services
docker service scale placement_app=3
```

### Using Kubernetes

```bash
# Convert to Kubernetes manifests
kompose convert

# Deploy to Kubernetes
kubectl apply -f .
```

### Using Cloud Platforms

#### AWS ECS
```bash
# Install ECS CLI
ecs-cli compose up

# Or use AWS Copilot
copilot init
copilot deploy
```

#### Google Cloud Run
```bash
# Build and push image
gcloud builds submit --tag gcr.io/PROJECT_ID/placement-app

# Deploy
gcloud run deploy placement-app --image gcr.io/PROJECT_ID/placement-app
```

---

## 📈 Performance Optimization

### Database Optimization

```sql
-- Add indexes
CREATE INDEX idx_applications_student ON applications(student_id);
CREATE INDEX idx_job_postings_deadline ON job_postings(deadline);

-- Optimize tables
OPTIMIZE TABLE users, students, applications;
```

### Application Optimization

```yaml
# Increase memory
environment:
  CATALINA_OPTS: "-Xms1024m -Xmx2048m -XX:+UseG1GC"

# Enable connection pooling
environment:
  DB_POOL_SIZE: 20
  DB_MAX_IDLE: 10
```

### Docker Optimization

```dockerfile
# Use multi-stage builds (already implemented)
# Use layer caching
# Minimize image size
```

---

## 🔄 Updates and Maintenance

### Update Application

```bash
# Pull latest code
git pull

# Rebuild and restart
docker compose up --build -d

# Or rolling update
docker compose up -d --no-deps --build app
```

### Update Database Schema

```bash
# Apply migration
docker compose exec db mysql -u root -p placement_system < sql/migration.sql

# Or use Flyway/Liquibase
```

### Backup Strategy

```bash
# Automated backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker compose exec db mysqldump -u root -p placement_system > backup_$DATE.sql
```

---

## 📞 Support

### Common Issues
- Check logs: `docker compose logs`
- Verify configuration: `docker compose config`
- Restart services: `docker compose restart`

### Resources
- Docker Documentation: https://docs.docker.com
- Docker Compose: https://docs.docker.com/compose
- Tomcat Documentation: https://tomcat.apache.org/tomcat-10.0-doc

---

## ✅ Checklist

Before deployment:
- [ ] Environment variables configured
- [ ] Database credentials changed
- [ ] SMTP settings configured (if using email)
- [ ] Ports available (8080, 3306)
- [ ] Docker and Docker Compose installed
- [ ] Sufficient disk space (2GB+)
- [ ] Sufficient memory (2GB+)

After deployment:
- [ ] Application accessible at http://localhost:8080
- [ ] Database initialized successfully
- [ ] Admin login works
- [ ] Health checks passing
- [ ] Logs show no errors

---

**Deployment Status**: ✅ Ready for Production

**Last Updated**: April 18, 2026
