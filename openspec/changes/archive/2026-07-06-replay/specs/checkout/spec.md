## ADDED Requirements

### Requirement: Order and OrderItem Entities
The system SHALL store orders with fields: id, user (ManyToOne), orderDate (LocalDateTime, auto-set on creation), status (OrderStatus enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED), totalAmount (BigDecimal), transactionId (String, nullable), shippingAddress (String). OrderItem SHALL have fields: id, order (ManyToOne), product (ManyToOne), quantity (int), unitPrice (BigDecimal, snapshot of the price at order time).

#### Scenario: Order status transitions
- **WHEN** an order is created
- **THEN** it SHALL have status PENDING
- **AND** after successful payment, it SHALL be updated to CONFIRMED
- **AND** an admin can change status to SHIPPED, DELIVERED, or CANCELLED

### Requirement: Create Order from Cart
The system SHALL allow authenticated users to create an order via POST /api/orders with shippingAddress in the request body. The system SHALL copy all CartItems into OrderItems (snapshotting the current unit price), calculate totalAmount as the sum of (quantity * unitPrice) for all items, set orderDate to now, set status to PENDING, and clear the cart. The stock is NOT decremented at this stage.

#### Scenario: Create order from non-empty cart
- **WHEN** a user with items in their cart sends POST /api/orders with body { "shippingAddress": "15 rue du Gaming, 75001 Paris" }
- **THEN** the system creates an Order with status PENDING, copies all CartItems to OrderItems, clears the cart, and returns HTTP 201 with OrderDTO

#### Scenario: Create order from empty cart
- **WHEN** a user with an empty cart sends POST /api/orders
- **THEN** the system returns HTTP 400 with error "Cart is empty"

#### Scenario: Price snapshot in OrderItem
- **WHEN** an order is created and a product's unitPrice is 29.99
- **THEN** the OrderItem SHALL store unitPrice=29.99 regardless of future price changes to the product

### Requirement: Fake Payment
The system SHALL provide a POST /api/payments/pay endpoint that simulates a payment. It SHALL accept orderId and paymentMethod (CARTE, PAYPAL, VIREMENT) in the request body. It SHALL verify the order exists and belongs to the authenticated user, verify the order status is PENDING, verify stock is sufficient for every OrderItem, decrement stock for each product, set the order status to CONFIRMED, generate a transactionId (prefix TXN- followed by a UUID), store it in the order, and return a PaymentResponse with success, transactionId, and orderId.

#### Scenario: Successful payment
- **WHEN** a user sends POST /api/payments/pay with { "orderId": 42, "method": "CARTE" }
- **THEN** the system decrements stock for each product in the order, sets order status to CONFIRMED, generates a transactionId like "TXN-a1b2c3d4-...", and returns HTTP 200 with { "success": true, "transactionId": "TXN-a1b2c3d4-...", "orderId": 42 }

#### Scenario: Payment with insufficient stock
- **WHEN** a user tries to pay an order where a product's current stock is less than the ordered quantity
- **THEN** the system returns HTTP 400 with error "Insufficient stock for product: <product-name>. Available: X, Requested: Y"

#### Scenario: Pay already paid order
- **WHEN** a user tries to pay an order that has status CONFIRMED (or any non-PENDING status)
- **THEN** the system returns HTTP 400 with error "Order cannot be paid. Current status: CONFIRMED"

#### Scenario: Pay another user's order
- **WHEN** a user tries to pay an order that belongs to another user
- **THEN** the system returns HTTP 403 Forbidden

### Requirement: Order History
The system SHALL provide a GET /api/orders endpoint for authenticated users to retrieve their order history, ordered by orderDate descending. It SHALL support pagination (page, size).

#### Scenario: Get user's order history
- **WHEN** an authenticated user sends GET /api/orders?page=0&size=10
- **THEN** the system returns a paginated list of OrderSummaryDTO (id, orderDate, status, totalAmount, transactionId if CONFIRMED, item count) for that user's orders

#### Scenario: Multiple orders returned
- **WHEN** a user has 12 orders and requests page 0 with size 10
- **THEN** the system returns 10 orders with totalPages=2

### Requirement: Order Detail
The system SHALL provide a GET /api/orders/{id} endpoint returning the complete order details including all OrderItems with product information. The user SHALL only be able to view their own orders (unless ADMIN).

#### Scenario: Get own order detail
- **WHEN** a user sends GET /api/orders/42 (their own order)
- **THEN** the system returns OrderDetailDTO with order fields and an items array (productName, productSlug, imageUrl, quantity, unitPrice, subtotal per item)

#### Scenario: Get another user's order
- **WHEN** a user with role USER sends GET /api/orders/99 (another user's order)
- **THEN** the system returns HTTP 403 Forbidden