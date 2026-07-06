## ADDED Requirements

### Requirement: Admin Statistics Dashboard
The system SHALL provide a GET /api/admin/stats endpoint (restricted to ADMIN role) returning aggregated statistics: totalUsers (count of all users), totalOrders (count of all orders), totalRevenue (sum of totalAmount for all CONFIRMED orders), totalProducts (count of non-archived products), and topProducts (top 5 products by total quantity sold in CONFIRMED orders, each with productId, productName, and totalSold).

#### Scenario: Admin requests statistics
- **WHEN** an admin user sends GET /api/admin/stats
- **THEN** the system returns HTTP 200 with a StatsDTO containing all the aggregated values

#### Scenario: Non-admin requests statistics
- **WHEN** a user with role USER sends GET /api/admin/stats
- **THEN** the system returns HTTP 403 Forbidden

### Requirement: Admin Product Management
The system SHALL provide CRUD endpoints for products under /api/admin/products, restricted to ADMIN role. The endpoints SHALL allow: listing all products including archived (GET, with same pagination and filters as the public endpoint but including archived), creating a new product (POST, with all required product fields), updating an existing product (PUT /api/admin/products/{id}), and archiving a product (PUT with archived=true, which is a soft delete).

#### Scenario: Admin lists all products including archived
- **WHEN** an admin sends GET /api/admin/products?page=0&size=20
- **THEN** the system returns all products including those with archived=true

#### Scenario: Admin creates a new product
- **WHEN** an admin sends POST /api/admin/products with valid product data (name, description, price, condition, consoleType, stockQuantity, categoryId)
- **THEN** the system creates the product, generates a slug from the name, sets a default imageUrl from picsum.photos, and returns HTTP 201 with the created ProductDTO

#### Scenario: Admin creates product with duplicate slug
- **WHEN** an admin sends POST /api/admin/products with a name that would generate an already-existing slug
- **THEN** the system returns HTTP 409 with error "A product with this name already exists"

#### Scenario: Admin updates a product
- **WHEN** an admin sends PUT /api/admin/products/5 with updated fields
- **THEN** the system updates the product and returns HTTP 200 with the updated ProductDTO

#### Scenario: Admin archives a product
- **WHEN** an admin sends PUT /api/admin/products/5 with archived=true
- **THEN** the product is soft-deleted (archived=true), it no longer appears in public catalog endpoints

### Requirement: Admin Category Management
The system SHALL provide CRUD endpoints for categories under /api/admin/categories, restricted to ADMIN role. A category with associated products SHALL NOT be deleted.

#### Scenario: Admin creates a category
- **WHEN** an admin sends POST /api/admin/categories with name, description, and imageUrl
- **THEN** the system creates the category, generates a slug, and returns HTTP 201

#### Scenario: Admin updates a category
- **WHEN** an admin sends PUT /api/admin/categories/3 with updated name and description
- **THEN** the system updates the category (re-generating slug if name changed) and returns HTTP 200

#### Scenario: Admin deletes category without products
- **WHEN** an admin sends DELETE /api/admin/categories/5 and the category has no associated products
- **THEN** the system deletes the category and returns HTTP 200

#### Scenario: Admin tries to delete category with products
- **WHEN** an admin sends DELETE /api/admin/categories/3 and the category has associated products
- **THEN** the system returns HTTP 400 with error "Cannot delete category with associated products"

### Requirement: Admin Order Management
The system SHALL provide endpoints for admins to list all orders (GET /api/admin/orders with pagination and optional status filter) and to update an order's status (PUT /api/admin/orders/{id}/status with the new status in the request body). The status SHALL only change between valid transitions: PENDING to CONFIRMED or CANCELLED, CONFIRMED to SHIPPED or CANCELLED, SHIPPED to DELIVERED.

#### Scenario: Admin lists all orders
- **WHEN** an admin sends GET /api/admin/orders?page=0&size=20&status=CONFIRMED
- **THEN** the system returns a paginated list of OrderDTO for all users, filtered by status CONFIRMED

#### Scenario: Admin changes order status
- **WHEN** an admin sends PUT /api/admin/orders/42/status with body { "status": "SHIPPED" }
- **THEN** the system updates the order status to SHIPPED and returns HTTP 200

#### Scenario: Admin tries invalid status transition
- **WHEN** an admin sends PUT /api/admin/orders/42/status with body { "status": "PENDING" } and the order is already CONFIRMED
- **THEN** the system returns HTTP 400 with error "Invalid status transition from CONFIRMED to PENDING"

### Requirement: Admin User Management
The system SHALL provide a GET /api/admin/users endpoint returning a paginated list of all users (UserDTO with id, email, firstName, lastName, role, createdAt, but NO password hash).

#### Scenario: Admin lists all users
- **WHEN** an admin sends GET /api/admin/users?page=0&size=20
- **THEN** the system returns a paginated list of UserDTO objects

### Requirement: Seed Data for Development
The system SHALL include a DataInitializer (CommandLineRunner) that seeds the database with demo data on startup if the database is empty. This includes: one admin user (admin@replay.fr / admin123, role ADMIN), at least 5 categories (e.g., Consoles, Jeux, Manettes, Accessoires, Figurines), and at least 20 products distributed across categories with various console types, conditions, prices (10-500 EUR), and stock quantities (1-50). The seed data SHALL only run when the user table is empty (to avoid duplicate data on restart).

#### Scenario: First application startup with empty database
- **WHEN** the Spring Boot application starts and the users table is empty
- **THEN** the DataInitializer seeds the admin user, categories, and demo products using picsum.photos for images

#### Scenario: Subsequent startups
- **WHEN** the Spring Boot application starts and the users table already contains data
- **THEN** the DataInitializer does nothing (no duplicate seeding)