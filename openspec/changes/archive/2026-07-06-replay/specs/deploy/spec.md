## ADDED Requirements

### Requirement: Docker Compose Development Environment
The system SHALL include a docker-compose.yml at the project root defining at least two services: postgres (PostgreSQL 16 with env vars POSTGRES_DB=replay, POSTGRES_USER=replay, POSTGRES_PASSWORD=replay, port 5432, volume for persistent data) and optionally pgadmin (pgAdmin 4 for database inspection, port 5050). The backend application SHALL run locally outside Docker during development (connected to the Docker PostgreSQL instance).

#### Scenario: Start development environment
- **WHEN** a developer runs `docker compose up -d` from the project root
- **THEN** PostgreSQL 16 starts, accessible at localhost:5432 with database replay, and data persists across restarts via a named volume

#### Scenario: Application connects to Docker PostgreSQL
- **WHEN** the Spring Boot application starts with the dev profile and spring.datasource.url pointing to jdbc:postgresql://localhost:5432/replay
- **THEN** the application successfully connects to the Docker PostgreSQL instance

### Requirement: Backend Production Dockerfile
The system SHALL provide a backend/Dockerfile using multi-stage build: Stage 1 builds the Spring Boot JAR with Maven and JDK 17, Stage 2 copies the JAR into a lightweight JRE 17 runtime image (eclipse-temurin:17-jre or similar). The image SHALL expose port 8080 and start the Spring Boot application with the prod profile.

#### Scenario: Backend Docker build
- **WHEN** `docker build -t replay-backend ./backend` is executed
- **THEN** the multi-stage build produces an image with the Spring Boot JAR and a JRE 17 runtime, exposing port 8080

### Requirement: Frontend Production Dockerfile
The system SHALL provide a frontend/Dockerfile using multi-stage build: Stage 1 builds the Angular application with Node 20 (npm ci && npm run build), Stage 2 copies the compiled files from dist/ into an Nginx Alpine image. Nginx SHALL be configured to serve the static files on port 80, with a fallback to index.html for Angular routing (try_files $uri $uri/ /index.html).

#### Scenario: Frontend Docker build
- **WHEN** `docker build -t replay-frontend ./frontend` is executed
- **THEN** the multi-stage build produces an Nginx image serving the Angular application on port 80

#### Scenario: Angular routing in Docker
- **WHEN** a browser requests a deep link like /products/super-mario-64 from the deployed Docker frontend
- **THEN** Nginx serves index.html (Angular router handles the path client-side)

### Requirement: Production Docker Compose
The system SHALL provide a docker-compose.prod.yml (or documented command) for the production environment, defining three services: postgres (with stronger password from env variable), backend (replay-backend image, exposed on port 8080), and frontend (replay-frontend image, exposed on port 80). The backend service SHALL depend on postgres.

#### Scenario: Production deployment
- **WHEN** `docker compose -f docker-compose.prod.yml up -d` is executed on the production server
- **THEN** all three services start, the frontend is accessible on port 80, and the backend API on port 8080

### Requirement: Oracle Cloud Deployment Instructions
The system SHALL include a deployment guide (deploy/deploy.md or documented in README) explaining how to deploy on an Oracle Cloud Free Tier Ampere A1 instance: create the VM (Ubuntu 22.04, Ampere A1 shape), install Docker and Docker Compose, clone the repository, configure environment variables, pull the Docker images, and run the production Docker Compose stack.

#### Scenario: Follow deployment guide
- **WHEN** following the deployment instructions step by step
- **THEN** the application is accessible via the VM's public IP address on port 80 (frontend) and port 8080 (backend API)