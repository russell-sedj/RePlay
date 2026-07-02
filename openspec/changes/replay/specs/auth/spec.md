## ADDED Requirements

### Requirement: User Registration
The system SHALL allow new users to register with email, password, first name and last name. The password MUST be at least 8 characters. The email MUST be unique. The password SHALL be hashed with BCrypt before storage. New users SHALL be assigned the USER role by default.

#### Scenario: Successful registration
- **WHEN** a visitor sends a POST to /api/auth/register with a valid email (not already registered), password (8+ chars), first name and last name
- **THEN** the system creates a new user with role USER, returns HTTP 201 with a UserProfileDTO (id, email, firstName, lastName, role) and no password field

#### Scenario: Duplicate email registration
- **WHEN** a visitor sends a POST to /api/auth/register with an email already existing in the database
- **THEN** the system returns HTTP 409 with an error message "Email already registered"

#### Scenario: Short password registration
- **WHEN** a visitor sends a POST to /api/auth/register with a password shorter than 8 characters
- **THEN** the system returns HTTP 400 with a validation error message

### Requirement: User Login
The system SHALL authenticate users by email and password. On successful authentication, it SHALL return an access token (JWT, 15 minute expiry) and a refresh token (JWT, 7 day expiry). The refresh token SHALL be stored in the user's database record.

#### Scenario: Successful login
- **WHEN** a registered user sends a POST to /api/auth/login with valid email and password
- **THEN** the system returns HTTP 200 with a JSON containing accessToken (JWT, 15min), refreshToken (JWT, 7 days), and a UserProfileDTO
- **AND** the refreshToken is saved in the users table refresh_token column

#### Scenario: Invalid credentials
- **WHEN** a user sends a POST to /api/auth/login with an incorrect password or non-existent email
- **THEN** the system returns HTTP 401 with an error message "Invalid credentials"

### Requirement: Token Refresh
The system SHALL allow users to obtain a new access token by providing a valid refresh token. The old refresh token SHALL be invalidated and a new one issued (rotation). If the refresh token is invalid or expired, the request SHALL be rejected.

#### Scenario: Successful token refresh
- **WHEN** an authenticated client sends a POST to /api/auth/refresh with a valid, non-expired refreshToken in the request body
- **THEN** the system returns HTTP 200 with a new accessToken and a new refreshToken
- **AND** the old refreshToken in the database is replaced by the new one

#### Scenario: Invalid or expired refresh token
- **WHEN** a client sends a POST to /api/auth/refresh with an invalid, tampered, or expired refreshToken
- **THEN** the system returns HTTP 401 with an error message "Invalid or expired refresh token"

### Requirement: Current User Profile
The system SHALL provide an endpoint for the authenticated user to retrieve their own profile information, excluding the password hash.

#### Scenario: Authenticated user requests profile
- **WHEN** an authenticated user sends a GET to /api/auth/me with a valid Authorization header
- **THEN** the system returns HTTP 200 with UserProfileDTO (id, email, firstName, lastName, role)

#### Scenario: Unauthenticated profile request
- **WHEN** a request without Authorization header is sent to GET /api/auth/me
- **THEN** the system returns HTTP 401

### Requirement: Role-Based Access Control
The system SHALL support two roles: USER and ADMIN. All endpoints under /api/admin/ SHALL require the ADMIN role. Non-admin endpoints SHALL require authentication (USER or ADMIN). Endpoints under /api/auth/ and the public catalog endpoints (/api/products, /api/categories) SHALL be accessible without authentication.

#### Scenario: USER accesses admin endpoint
- **WHEN** a user with role USER sends a request to any /api/admin/** endpoint
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Unauthenticated access to protected endpoint
- **WHEN** a request without a valid JWT is sent to GET /api/cart or any other protected non-admin endpoint
- **THEN** the system returns HTTP 401

### Requirement: JWT Access Token Validation
Every authenticated request SHALL include a valid JWT access token in the Authorization header as Bearer token. The token SHALL contain the user's email and role as claims. If the token is expired, missing, or invalid, the request SHALL be rejected with HTTP 401.

#### Scenario: Valid token in Authorization header
- **WHEN** a request includes header "Authorization: Bearer <valid-access-token>"
- **THEN** the JwtAuthFilter extracts and validates the token, sets the SecurityContext with the user's authentication

#### Scenario: Expired access token
- **WHEN** a request includes an access token that has expired (after 15 minutes)
- **THEN** the system returns HTTP 401

### Requirement: Security Configuration
The Spring Security configuration SHALL define a SecurityFilterChain with: stateless session management, CORS configuration allowing the Angular frontend origin (http://localhost:4200), CSRF disabled, public access to /api/auth/**, /api/products/**, /api/categories/**, /swagger-ui/**, /v3/api-docs/**, /api/admin/** restricted to ADMIN role, and all other endpoints requiring authentication.

#### Scenario: CORS preflight request
- **WHEN** the Angular frontend at http://localhost:4200 sends an OPTIONS preflight request
- **THEN** the system responds with appropriate CORS headers (Access-Control-Allow-Origin, Allow-Methods, Allow-Headers)