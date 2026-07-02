## ADDED Requirements

### Requirement: Cart Entity and Relationship
The system SHALL store a Cart for each user that has added items to their cart. A User SHALL have exactly zero or one Cart (OneToOne relationship). A Cart SHALL contain zero or more CartItems (OneToMany). A CartItem SHALL reference a Product (ManyToOne) and have an integer quantity field (minimum 1). The combination of (cart, product) SHALL be unique (one CartItem per product per cart).

#### Scenario: Cart auto-creation on first item
- **WHEN** a user adds their first item to their cart
- **THEN** the system creates a new Cart entity linked to the user

#### Scenario: No cart for new user
- **WHEN** a newly registered user requests their cart
- **THEN** the system returns an empty cart (no Cart entity exists yet in the database)

### Requirement: Get Cart
The system SHALL provide a GET /api/cart endpoint for authenticated users. It SHALL return the current user's cart with all cart items including product details (name, slug, price, imageUrl) and subtotal per item (quantity * unit price). If the user has no cart yet, it SHALL return an empty cart with total=0 and an empty items array.

#### Scenario: Get existing cart
- **WHEN** an authenticated user with items in their cart sends a GET to /api/cart
- **THEN** the system returns HTTP 200 with CartDTO containing items array (each with productId, productName, productSlug, imageUrl, unitPrice, quantity, subtotal) and total (sum of all subtotals)

#### Scenario: Get empty cart for user without cart
- **WHEN** an authenticated user who has never added items sends a GET to /api/cart
- **THEN** the system returns HTTP 200 with an empty CartDTO (items: [], total: 0, isEmpty: true)

### Requirement: Add Item to Cart
The system SHALL allow authenticated users to add a product to their cart via POST /api/cart/items with productId and quantity in the request body. If the product is already in the cart, the quantity SHALL be incremented (not duplicated). The quantity added SHALL be at least 1. The system SHALL verify the requested quantity does not exceed available stock, but SHALL NOT decrement the stock (stock is decremented at checkout).

#### Scenario: Add new product to cart
- **WHEN** a user sends POST /api/cart/items with productId=5 and quantity=2 and the product stock is 10
- **THEN** the system creates a new CartItem with quantity=2 and returns HTTP 201 with the updated CartDTO

#### Scenario: Add existing product increments quantity
- **WHEN** a user has product 5 in their cart with quantity 2, and sends POST /api/cart/items with productId=5 and quantity=1
- **THEN** the system updates the existing CartItem to quantity=3 and returns the updated CartDTO

#### Scenario: Add quantity exceeding stock
- **WHEN** a user sends POST /api/cart/items with productId=5 and quantity=15 and the product stock is 10
- **THEN** the system returns HTTP 400 with error "Not enough stock available. Current stock: 10"

#### Scenario: Add non-existent product
- **WHEN** a user sends POST /api/cart/items with productId that does not exist in the database
- **THEN** the system returns HTTP 404 with error "Product not found"

### Requirement: Update Cart Item Quantity
The system SHALL allow users to update the quantity of a cart item via PUT /api/cart/items/{itemId} with a new quantity. The quantity SHALL be at least 1. The stock SHALL be checked again for the new total quantity.

#### Scenario: Update quantity within stock
- **WHEN** a user sends PUT /api/cart/items/12 with quantity=5 and the product stock is 20
- **THEN** the system updates the CartItem quantity to 5 and returns the updated CartDTO

#### Scenario: Update quantity exceeding stock
- **WHEN** a user sends PUT /api/cart/items/12 with quantity=50 and the product stock is 20
- **THEN** the system returns HTTP 400 with error "Not enough stock available"

### Requirement: Remove Item from Cart
The system SHALL allow users to remove a cart item via DELETE /api/cart/items/{itemId}. If the Cart is empty after removal, it SHALL NOT be deleted (the Cart entity persists).

#### Scenario: Remove existing cart item
- **WHEN** a user sends DELETE /api/cart/items/12
- **THEN** the CartItem is removed, and the system returns the updated CartDTO

#### Scenario: Remove non-existing or foreign cart item
- **WHEN** a user sends DELETE /api/cart/items/999 or an itemId belonging to another user's cart
- **THEN** the system returns HTTP 404 or 403 accordingly

### Requirement: Clear Cart
The system SHALL allow users to empty their entire cart via DELETE /api/cart. This SHALL delete all CartItems but keep the Cart entity.

#### Scenario: Clear populated cart
- **WHEN** a user with items in their cart sends DELETE /api/cart
- **THEN** all CartItems are removed, and the system returns an empty CartDTO (items: [], total: 0)