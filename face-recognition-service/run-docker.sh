#!/bin/bash

# Script to run Face Recognition Service with Docker

echo "========================================="
echo "RideMate Face Recognition Service"
echo "========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

echo "✓ Docker is installed"
echo ""

# Build Docker image
echo "📦 Building Docker image..."
docker build -t ridemate-face-recognition:latest .

if [ $? -ne 0 ]; then
    echo "❌ Failed to build Docker image"
    exit 1
fi

echo "✓ Docker image built successfully"
echo ""

# Stop and remove existing container if running
echo "🔄 Checking for existing container..."
if [ "$(docker ps -aq -f name=ridemate-face-recognition)" ]; then
    echo "Stopping and removing existing container..."
    docker stop ridemate-face-recognition 2>/dev/null
    docker rm ridemate-face-recognition 2>/dev/null
fi

echo ""
echo "🚀 Starting Face Recognition Service..."
echo ""

# Run container
docker run -d \
  --name ridemate-face-recognition \
  -p 5000:5000 \
  --restart unless-stopped \
  ridemate-face-recognition:latest

if [ $? -ne 0 ]; then
    echo "❌ Failed to start container"
    exit 1
fi

echo "✓ Container started successfully"
echo ""

# Wait for service to be ready
echo "⏳ Waiting for service to be ready..."
sleep 5

# Check health
echo "🔍 Checking service health..."
HEALTH_CHECK=$(curl -s http://localhost:5000/health)

if [ $? -eq 0 ]; then
    echo "✓ Service is healthy!"
    echo ""
    echo "Response: $HEALTH_CHECK"
    echo ""
    echo "========================================="
    echo "✅ Face Recognition Service is running!"
    echo "========================================="
    echo ""
    echo "Service URL: http://localhost:5000"
    echo "Container name: ridemate-face-recognition"
    echo ""
    echo "Useful commands:"
    echo "  - View logs: docker logs -f ridemate-face-recognition"
    echo "  - Stop service: docker stop ridemate-face-recognition"
    echo "  - Restart service: docker restart ridemate-face-recognition"
    echo ""
else
    echo "⚠️  Service started but health check failed"
    echo "Check logs with: docker logs ridemate-face-recognition"
fi
