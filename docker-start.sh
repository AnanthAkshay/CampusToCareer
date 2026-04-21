#!/bin/bash
# ============================================================================
# Quick Start Script for RIT Placement Portal
# ============================================================================

set -e

echo "🐳 RIT Placement Portal - Docker Quick Start"
echo "=============================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "   Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    echo "   Visit: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from template..."
    cp .env.example .env
    echo "✅ Created .env file. Please review and update if needed."
    echo ""
fi

# Check if ports are available
echo "🔍 Checking if ports are available..."

if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "❌ Port 8080 is already in use. Please stop the service using it."
    exit 1
fi

if lsof -Pi :3306 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  Port 3306 is already in use. This might be a local MySQL instance."
    echo "   The Docker MySQL will still work on the internal network."
fi

echo "✅ Ports are available"
echo ""

# Build and start services
echo "🚀 Building and starting services..."
echo "   This may take a few minutes on first run..."
echo ""

docker compose up --build -d

echo ""
echo "⏳ Waiting for services to be healthy..."
sleep 10

# Check service status
if docker compose ps | grep -q "Up"; then
    echo ""
    echo "✅ Services are running!"
    echo ""
    echo "📊 Service Status:"
    docker compose ps
    echo ""
    echo "🌐 Application URL: http://localhost:8080"
    echo "🗄️  Database: localhost:3306"
    echo ""
    echo "👤 Default Login:"
    echo "   Username: ADMIN001"
    echo "   Password: admin123"
    echo ""
    echo "📝 View logs: docker compose logs -f"
    echo "🛑 Stop services: docker compose down"
    echo ""
else
    echo ""
    echo "❌ Services failed to start. Check logs:"
    echo "   docker compose logs"
    exit 1
fi
