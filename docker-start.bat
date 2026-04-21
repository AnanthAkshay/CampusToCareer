@echo off
REM ============================================================================
REM Quick Start Script for RIT Placement Portal (Windows)
REM ============================================================================

echo.
echo 🐳 RIT Placement Portal - Docker Quick Start
echo ==============================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not installed. Please install Docker Desktop first.
    echo    Visit: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker compose version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose is not installed. Please install Docker Desktop first.
    echo    Visit: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo ✅ Docker and Docker Compose are installed
echo.

REM Check if .env file exists
if not exist .env (
    echo ⚠️  .env file not found. Creating from template...
    copy .env.example .env >nul
    echo ✅ Created .env file. Please review and update if needed.
    echo.
)

REM Check if ports are available
echo 🔍 Checking if ports are available...
netstat -ano | findstr :8080 | findstr LISTENING >nul 2>&1
if not errorlevel 1 (
    echo ❌ Port 8080 is already in use. Please stop the service using it.
    pause
    exit /b 1
)

echo ✅ Ports are available
echo.

REM Build and start services
echo 🚀 Building and starting services...
echo    This may take a few minutes on first run...
echo.

docker compose up --build -d

echo.
echo ⏳ Waiting for services to be healthy...
timeout /t 10 /nobreak >nul

REM Check service status
docker compose ps | findstr "Up" >nul 2>&1
if not errorlevel 1 (
    echo.
    echo ✅ Services are running!
    echo.
    echo 📊 Service Status:
    docker compose ps
    echo.
    echo 🌐 Application URL: http://localhost:8080
    echo 🗄️  Database: localhost:3306
    echo.
    echo 👤 Default Login:
    echo    Username: ADMIN001
    echo    Password: admin123
    echo.
    echo 📝 View logs: docker compose logs -f
    echo 🛑 Stop services: docker compose down
    echo.
) else (
    echo.
    echo ❌ Services failed to start. Check logs:
    echo    docker compose logs
    pause
    exit /b 1
)

pause
