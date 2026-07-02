## ADDED Requirements

### Requirement: GitHub Actions Workflow Structure
The system SHALL include a CI workflow at .github/workflows/ci.yml triggered on push and pull requests to the main branch. The workflow SHALL have two parallel jobs: backend (build and test the Spring Boot application) and frontend (build and lint the Angular application).

#### Scenario: Push triggers CI
- **WHEN** a commit is pushed to the main branch or a pull request is opened against main
- **THEN** the CI workflow runs both backend and frontend jobs in parallel

### Requirement: Backend CI Job
The backend job SHALL run on ubuntu-latest with Java 17 (Temurin). It SHALL execute: mvn verify (which compiles, runs tests with JUnit and Mockito) and mvn package -DskipTests (to build the final JAR, optional for cache). The job SHALL use actions/cache for Maven dependencies to speed up subsequent runs.

#### Scenario: Backend tests pass
- **WHEN** backend code is pushed with all tests passing
- **THEN** the backend job completes successfully (green)

#### Scenario: Backend tests fail
- **WHEN** backend code is pushed with a failing test
- **THEN** the backend job fails (red) and the overall workflow status is failure

### Requirement: Frontend CI Job
The frontend job SHALL run on ubuntu-latest with Node.js 20. It SHALL execute: npm ci, npm run build (Angular production build, which includes TypeScript compilation), and npm run lint (TypeScript linting, if configured). It SHALL use actions/cache for npm dependencies.

#### Scenario: Frontend build succeeds
- **WHEN** frontend code is pushed with valid TypeScript
- **THEN** the frontend job completes successfully

#### Scenario: Frontend TypeScript compilation error
- **WHEN** frontend code is pushed with a TypeScript error
- **THEN** the frontend job fails and the overall workflow status is failure

### Requirement: Docker Multi-Arch Build
The CI workflow SHALL include a docker-build job that runs after both backend and frontend jobs succeed. It SHALL use docker buildx to build backend and frontend Docker images for both linux/amd64 and linux/arm64 platforms. The images SHALL be tagged with the commit SHA and optionally pushed to a container registry (GitHub Container Registry or Docker Hub) on the main branch.

#### Scenario: Docker images build after successful tests
- **WHEN** both backend and frontend jobs pass on the main branch
- **THEN** the docker-build job builds multi-arch images and pushes them to the container registry

#### Scenario: Docker images build on pull request
- **WHEN** both backend and frontend jobs pass on a pull request
- **THEN** the docker-build job builds the images (to verify they compile) but does NOT push them to the registry