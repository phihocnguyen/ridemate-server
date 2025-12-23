# Docker Deployment Guide - Face Recognition Service

## Quick Start

### Option 1: Using run-docker.sh script (Recommended)

```bash
cd /home/hocnp/Desktop/ridemate-server/face-recognition-service
./run-docker.sh
```

Script sẽ tự động:
- Build Docker image
- Stop container cũ (nếu có)
- Start container mới
- Check health

### Option 2: Manual Docker commands

```bash
cd /home/hocnp/Desktop/ridemate-server/face-recognition-service

# Build image
docker build -t ridemate-face-recognition:latest .

# Run container
docker run -d \
  --name ridemate-face-recognition \
  -p 5000:5000 \
  --restart unless-stopped \
  ridemate-face-recognition:latest

# Check logs
docker logs -f ridemate-face-recognition
```

### Option 3: Using docker-compose

```bash
cd /home/hocnp/Desktop/ridemate-server/face-recognition-service

# Start service
docker-compose up -d

# View logs
docker-compose logs -f

# Stop service
docker-compose down
```

## Configuration

### Backend (Spring Boot)

Update `.env` file:
```properties
# Khi chạy Docker container
FACE_SERVICE_URL=http://localhost:5000

# Khi chạy với docker-compose và backend cũng trong Docker
FACE_SERVICE_URL=http://ridemate-face-recognition:5000
```

### Frontend (React Native)

Update `.env` file:
```properties
# Development (local machine)
FACE_SERVICE_URL=http://localhost:5000

# Development (Android emulator)
FACE_SERVICE_URL=http://10.0.2.2:5000

# Development (iOS simulator)
FACE_SERVICE_URL=http://localhost:5000

# Development (Real device - use your machine's IP)
FACE_SERVICE_URL=http://192.168.1.100:5000
```

## Network Configuration

### Scenario 1: All services on host machine
```
Frontend (React Native) → Backend (Spring Boot) → Python Service (Docker)
     localhost:8080              localhost:5000
```

Backend `.env`:
```
FACE_SERVICE_URL=http://localhost:5000
```

### Scenario 2: Backend + Python in Docker
```
Frontend → Backend (Docker) → Python (Docker)
              ↓
         Docker network
```

Backend `.env`:
```
FACE_SERVICE_URL=http://ridemate-face-recognition:5000
```

Create docker network:
```bash
docker network create ridemate-network
docker run --network ridemate-network ...
```

## Useful Commands

### Container Management
```bash
# Start container
docker start ridemate-face-recognition

# Stop container
docker stop ridemate-face-recognition

# Restart container
docker restart ridemate-face-recognition

# Remove container
docker rm -f ridemate-face-recognition

# View logs
docker logs -f ridemate-face-recognition

# Execute command in container
docker exec -it ridemate-face-recognition bash
```

### Image Management
```bash
# List images
docker images | grep ridemate

# Remove image
docker rmi ridemate-face-recognition:latest

# Rebuild image
docker build --no-cache -t ridemate-face-recognition:latest .
```

### Health Check
```bash
# Check if service is running
curl http://localhost:5000/health

# Test face detection
curl -X POST -F "image=@test_image.jpg" http://localhost:5000/detect-face
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker logs ridemate-face-recognition

# Check if port is already in use
lsof -i :5000
netstat -tulpn | grep 5000

# Remove and recreate
docker rm -f ridemate-face-recognition
./run-docker.sh
```

### Backend can't connect to Python service
```bash
# Check if container is running
docker ps | grep ridemate-face-recognition

# Check network connectivity
docker exec ridemate-face-recognition curl http://localhost:5000/health

# From host machine
curl http://localhost:5000/health
```

### Image build fails
```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker build --no-cache -t ridemate-face-recognition:latest .
```

## Production Deployment

### Using Docker Compose (Recommended)

Create `docker-compose.yml` for all services:

```yaml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: ridemate_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - ridemate-network

  face-recognition:
    image: ridemate-face-recognition:latest
    ports:
      - "5000:5000"
    networks:
      - ridemate-network

  backend:
    image: ridemate-backend:latest
    ports:
      - "8080:8080"
    environment:
      - FACE_SERVICE_URL=http://face-recognition:5000
      - DB_URL=jdbc:postgresql://postgres:5432/ridemate_db
    depends_on:
      - postgres
      - face-recognition
    networks:
      - ridemate-network

networks:
  ridemate-network:
    driver: bridge

volumes:
  postgres_data:
```

Start all services:
```bash
docker-compose up -d
```

## Monitoring

### View resource usage
```bash
docker stats ridemate-face-recognition
```

### Check health endpoint
```bash
watch -n 5 'curl -s http://localhost:5000/health | jq'
```

### Export logs
```bash
docker logs ridemate-face-recognition > face-service.log 2>&1
```
