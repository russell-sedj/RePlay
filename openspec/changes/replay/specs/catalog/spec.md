## ADDED Requirements

### Requirement: Product Entity with All Fields
The system SHALL store products with the following fields: id (auto-generated Long), name (String, required), slug (String, unique, generated from name), description (String, required), price (BigDecimal, required, positive), condition (ProductCondition enum: NEUF, BON_ETAT, RECONDITIONNE), consoleType (ConsoleType enum), stockQuantity (int, default 0), imageUrl (String, default placeholder from picsum.photos), archived (boolean, default false), createdAt (LocalDateTime), and category (ManyToOne relationship to Category). The ProductCondition and ConsoleType enums SHALL be stored as strings in the database.

#### Scenario: ConsoleType enum values
- **WHEN** defining the ConsoleType enum
- **THEN** it SHALL include at minimum: NES, SNES, NINTENDO_64, GAMECUBE, WII, WII_U, SWITCH, GAMEBOY, GAMEBOY_COLOR, GAMEBOY_ADVANCE, NINTENDO_DS, NINTENDO_3DS, PLAYSTATION, PS2, PS3, PS4, PS5, XBOX, XBOX_360, XBOX_ONE, XBOX_SERIES, SEGA_MEGA_DRIVE, SEGA_SATURN, SEGA_DREAMCAST, NEC_PC_ENGINE, ATARI, AUTRE

### Requirement: Category Entity
The system SHALL store categories with the following fields: id (auto-generated Long), name (String, required, unique), slug (String, unique, generated from name), description (String), imageUrl (String, placeholder from picsum.photos). A product SHALL belong to exactly one category via a ManyToOne relationship.

#### Scenario: Product-category relationship
- **WHEN** a product is created or updated
- **THEN** it MUST reference an existing category
- **AND** the category SHALL NOT be deleted if it still has associated products

### Requirement: Product Catalog Endpoint with Filters
The system SHALL provide a GET /api/products endpoint that supports: pagination (page number, size, default size=20), sorting (by price or name, ascending or descending, default sort=name,asc), and optional filters: category (by category slug), console (by ConsoleType name), condition (by ProductCondition name), minPrice (BigDecimal), maxPrice (BigDecimal). The response SHALL include: content (array of ProductSummaryDTO), totalPages, totalElements, number (current page), size, sort. Archived products (archived=true) SHALL be excluded by default.

#### Scenario: Basic paginated catalog
- **WHEN** a GET request is sent to /api/products?page=0&size=20
- **THEN** the system returns the first 20 products (not archived), sorted by name ascending, with pagination metadata

#### Scenario: Filter by category and console
- **WHEN** a GET request is sent to /api/products?category=consoles&console=PS1&page=0&size=10
- **THEN** the system returns products belonging to the "consoles" category AND having consoleType=PLAYSTATION, up to 10 per page

#### Scenario: Filter by price range
- **WHEN** a GET request is sent to /api/products?minPrice=10&maxPrice=50&sort=price,asc
- **THEN** the system returns products with price between 10 and 50 (inclusive), sorted by price ascending

#### Scenario: Filter by condition
- **WHEN** a GET request is sent to /api/products?condition=NEUF
- **THEN** the system returns only products with condition NEUF

### Requirement: Product Detail Endpoint
The system SHALL provide a GET /api/products/{slug} endpoint returning a ProductDetailDTO with all product fields including the associated category details. If the product is archived, it SHALL be returned (accessible by direct URL).

#### Scenario: Product found by slug
- **WHEN** a GET request is sent to /api/products/super-mario-64
- **THEN** the system returns HTTP 200 with the complete product details including category name and slug

#### Scenario: Product not found
- **WHEN** a GET request is sent to /api/products/non-existent-slug
- **THEN** the system returns HTTP 404

### Requirement: Categories Endpoint
The system SHALL provide a GET /api/categories endpoint returning all categories with their id, name, slug, description and imageUrl.

#### Scenario: List all categories
- **WHEN** a GET request is sent to /api/categories
- **THEN** the system returns HTTP 200 with an array of CategoryDTO (id, name, slug, description, imageUrl) for all existing categories

### Requirement: Product Specifications for Filtering
The system SHALL use Spring Data JPA Specifications to implement the product filtering logic. Each filter (category, console, condition, price range) SHALL be an independent Specification that can be combined with AND logic.

#### Scenario: No filters applied
- **WHEN** a request has no filter parameters
- **THEN** the system applies only the archived=false Specification and returns all non-archived products

#### Scenario: Combined multiple filters
- **WHEN** a request has category=jeux, condition=RECONDITIONNE, and console=NINTENDO_DS
- **THEN** all three Specifications are combined with AND logic, returning products matching all three criteria