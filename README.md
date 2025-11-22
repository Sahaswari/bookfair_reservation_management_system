# International Bookfair Reservation Management System

A comprehensive microservices-based reservation management system for international book fairs, built with Spring Boot backend services and separate frontend portals for employees and users.

## 📋 Table of Contents

- [Architecture](#architecture)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Quick Start with Docker](#quick-start-with-docker)
- [Service URLs](#service-urls)
- [Development](#development)
- [Documentation](#documentation)
- [Project Structure](#project-structure)

## 🏗️ Architecture

This system follows a microservices architecture with:

- **6 Backend Microservices** (Spring Boot with Java 17)
- **2 Frontend Portals** (Employee Portal and User Portal)
- **API Gateway** for routing and load balancing
- **Docker containerization** for each service
- **Shared Docker network** for inter-service communication

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                        │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Employee   │  │     User     │  │              │ │
│  │    Portal    │  │    Portal    │  │ API Gateway  │ │
│  │  Port: 3000  │  │  Port: 3001  │  │  Port: 8080  │ │
│  └──────────────┘  └──────────────┘  └──────┬───────┘ │
│                                              │          │
│  ┌───────────────────────────────────────────┘         │
│  │                                                      │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │  │     Auth     │  │    Genre     │  │  Notif.  │ │
│  │  │   Service    │  │   Service    │  │ Service  │ │
│  │  │  Port: 8081  │  │  Port: 8082  │  │Port: 8083│ │
│  │  └──────────────┘  └──────────────┘  └──────────┘ │
│  │                                                      │
│  │  ┌──────────────┐  ┌──────────────┐                │
│  │  │ Reservation  │  │    Stall     │                │
│  │  │   Service    │  │   Service    │                │
│  │  │  Port: 8084  │  │  Port: 8085  │                │
│  │  └──────────────┘  └──────────────┘                │
└──────────────────────────────────────────────────────────┘
```

## 🔧 Services

### Backend Services

| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Main entry point, routes requests to appropriate services |
| **Auth Service** | 8081 | Handles authentication and authorization |
| **Genre Service** | 8082 | Manages book genres and categories |
| **Notification Service** | 8083 | Sends notifications and alerts |
| **Reservation Service** | 8084 | Manages stall reservations |
| **Stall Service** | 8085 | Manages stall information and availability |

### Frontend Portals

| Portal | Port | Description |
|--------|------|-------------|
| **Employee Portal** | 3000 | Admin interface for managing the book fair |
| **User Portal** | 3001 | Public interface for browsing and making reservations |

## 📦 Prerequisites

- **Docker Desktop** (v20.10+) installed and running
- **Docker Compose** V2 (included with Docker Desktop)
- At least **4GB RAM** allocated to Docker
- Ports **8080-8085**, **3000**, and **3001** available

## 🚀 Quick Start with Docker

### Method 1: Using PowerShell Script (Recommended)

```powershell
# Start all services
.\docker-manage.ps1 up

# Check status
.\docker-manage.ps1 status

# View logs
.\docker-manage.ps1 logs

# Stop all services
.\docker-manage.ps1 down
```

### Method 2: Using Docker Compose

```powershell
# Build and start all services
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Backend Services Only

For frontend development, run only backend services:

```powershell
# Using the script
.\docker-manage.ps1 backend-only

# Or using Docker Compose
docker-compose -f docker-compose.backend.yml up -d
```

## 🌐 Service URLs

Once all containers are running:

- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8081
- **Genre Service**: http://localhost:8082
- **Notification Service**: http://localhost:8083
- **Reservation Service**: http://localhost:8084
- **Stall Service**: http://localhost:8085
- **Employee Portal**: http://localhost:3000
- **User Portal**: http://localhost:3001

## 💻 Development

### Running Services Locally

For active development with hot reload:

```powershell
# 1. Start only backend services in Docker
docker-compose -f docker-compose.backend.yml up -d

# 2. Run frontend locally
cd frontend/employee-portal
npm start

# Or for user portal
cd frontend/user-portal
npm start
```

### Rebuilding a Specific Service

```powershell
# Rebuild and restart a service
docker-compose up -d --build api-gateway

# Or using the management script
.\docker-manage.ps1 build
```

### Viewing Service Logs

```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service

# Last 100 lines
docker-compose logs --tail=100 api-gateway
```

### Accessing Container Shell

```powershell
# Access a container's shell
docker-compose exec api-gateway sh

# Or for bash if available
docker-compose exec api-gateway bash
```

## 📚 Documentation

- **[DOCKER_README.md](DOCKER_README.md)** - Comprehensive Docker setup guide
- **[DOCKER_QUICK_REFERENCE.md](DOCKER_QUICK_REFERENCE.md)** - Quick command reference
- **[DOCKER_SETUP_SUMMARY.md](DOCKER_SETUP_SUMMARY.md)** - Setup summary and next steps
- **[application.properties.sample](application.properties.sample)** - Sample Spring Boot configuration

## 📁 Project Structure

```
.
├── backend/
│   ├── api-gateway/          # API Gateway service
│   │   ├── src/
│   │   ├── Dockerfile
│   │   ├── .dockerignore
│   │   └── pom.xml
│   ├── auth-service/         # Authentication service
│   ├── genre-service/        # Genre management service
│   ├── notification-service/ # Notification service
│   ├── reservation-service/  # Reservation management service
│   └── stall-service/        # Stall management service
│
├── frontend/
│   ├── employee-portal/      # Employee/Admin portal
│   │   ├── Dockerfile
│   │   ├── nginx.conf
│   │   └── .dockerignore
│   └── user-portal/          # Public user portal
│       ├── Dockerfile
│       ├── nginx.conf
│       └── .dockerignore
│
├── docker-compose.yml        # Main Docker Compose file
├── docker-compose.backend.yml # Backend services only
├── docker-manage.ps1         # PowerShell management script
├── .env.example              # Environment variables template
└── README.md                 # This file
```

## 🔧 Configuration

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Edit the `.env` file to set your environment-specific values.

### Backend Configuration

Each backend service can be configured via `application.properties`:

```properties
# Server configuration
server.port=8080
spring.application.name=api-gateway

# Database configuration (if needed)
spring.datasource.url=jdbc:postgresql://postgres:5432/bookfair_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

See `application.properties.sample` for more configuration options.

### Adding Databases

To add PostgreSQL, MySQL, or Redis, uncomment the relevant sections in `docker-compose.yml`:

```yaml
# Uncomment the database service you need
postgres:
  image: postgres:16-alpine
  ports:
    - "5432:5432"
  # ... rest of configuration
```

## 🛠️ Troubleshooting

### Port Already in Use

```powershell
# Find process using the port
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <pid> /F
```

### Container Won't Start

```powershell
# Check logs
docker-compose logs service-name

# Rebuild without cache
docker-compose build --no-cache service-name

# Restart Docker Desktop
```

### Clean Up Docker Resources

```powershell
# Using the management script
.\docker-manage.ps1 clean

# Or manually
docker-compose down -v
docker system prune -a
```

## 🧪 Testing

```powershell
# Run tests for backend services
cd backend/api-gateway
./mvnw test

# Or using Maven
mvn test
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👥 Authors

- **Team** - University of Ruhuna

## 🙏 Acknowledgments

- Spring Boot team for excellent microservices framework
- Docker for containerization platform
- All contributors to this project

## 📧 Support

For issues, questions, or contributions, please contact the development team or create an issue in the repository.

---

**Note**: Make sure Docker Desktop is running before executing any Docker commands. For detailed Docker setup instructions, see [DOCKER_README.md](DOCKER_README.md).
