# RePlay — Agent Context File

> Full-stack e-commerce platform for retro gaming (Spring Boot 3.4 + Angular 18)
> Target: Oracle Cloud Free Tier ARM64 (Ampere A1)
> Budget: Zero (no paid APIs, fake Stripe payment)

---

## 1. Project Identity

- **Name**: RePlay
- **Type**: Full-stack e-commerce SPA (single-page application)
- **Audience**: Retro gaming collectors in France (French UI)
- **Domain**: replay.fr (dev)
- **Repo**: `projects/RePlay`

## 2. Stack & Infrastructure

| Layer | Technology | Notes |
|-------|-----------|-------|
| Backend | Java 17+, Spring Boot 3.4.4 | Maven, JPA, Security, Lombok |
| Auth | Spring Security + JWT | Access token 15min, refresh 7d |
| DB | PostgreSQL 16 | Via Docker Compose |
| Frontend | Angular 18 + Tailwind CSS | Standalone components |
| API Docs | SpringDoc OpenAPI | `/swagger-ui.html` |
| Container | Docker Compose | Multi-stage builds |
| CI/CD | GitHub Actions | Multi-arch (amd64 + arm64) |
| Deploy | Oracle Cloud Free Tier | Ampere A1 (4 OCPU, 24GB RAM) |
| Payment | Fake (mock) | Transaction ID: TXN-UUID-xxx |

### Ports
- `:4200` — Angular dev (proxy `/api` → `:8080`)
- `:8080` — Spring Boot API
- `:5432` — PostgreSQL
- `:5050` — pgAdmin
- `:80` — Production frontend (nginx)

## 3. Key Architectural Decisions

1. **Refresh token**: stored in `localStorage` + DB column `users.refresh_token` for server-side invalidation
2. **Stock decrement**: at payment confirmation (NOT at cart add)
3. **Admin**: same SPA, lazy-loaded module with `AdminGuard` (role check)
4. **Product images**: Wikimedia Commons (Evan-Amos public domain photos) for consoles/controllers; Wikipedia fair-use box art for games; picsum fallback for generic accessories/goodies
5. **Payment**: fake/mock — no Stripe, generates UUID as transaction ID
6. **Exceptions**: project-specific (`DuplicateResourceException`, `UnauthorizedException`, `ResourceNotFoundException`) in `com.replay.common`
7. **CORS**: allows `localhost:4200` only
8. **No JavaScript/Node/Maven locally**: all builds via Docker containers on Windows

## 4. Project Structure

```
RePlay/
├── backend/
│   ├── src/main/java/com/replay/
│   │   ├── admin/         # AdminController, DTOs
│   │   ├── auth/          # User, Role, AuthController, AuthService, JwtService, JwtAuthFilter, SecurityConfig
│   │   ├── cart/          # Cart, CartItem, CartService, CartController, DTOs
│   │   ├── common/        # DuplicateResourceException, ResourceNotFoundException, etc.
│   │   ├── config/        # DataInitializer (seed data), OpenApiConfig, SecurityConfig
│   │   ├── order/         # Order, OrderItem, OrderService, OrderController, DTOs
│   │   ├── payment/       # PaymentController, PaymentService, PaymentResponse
│   │   └── product/       # Product, Category, ProductService, ProductController, DTOs, enums
│   └── Dockerfile
├── frontend/
│   ├── src/app/
│   │   ├── admin/         # AdminModule (lazy), DashboardComponent, ProductListComponent, etc.
│   │   ├── auth/          # LoginComponent, RegisterComponent, guards
│   │   ├── cart/          # CartService, CartPageComponent
│   │   ├── checkout/      # CheckoutComponent
│   │   ├── core/          # AuthService, auth.interceptor.ts
│   │   ├── order/         # OrderService, OrderListComponent, OrderDetailComponent
│   │   ├── products/      # ProductService, CatalogComponent, ProductDetailComponent
│   │   └── app.module.ts, app-routing.module.ts
│   ├── Dockerfile
│   └── proxy.conf.json
├── docker-compose.yml       # Dev: pg + pgadmin
├── docker-compose.prod.yml  # Prod: pg + backend:8080 + frontend:80
├── .github/workflows/ci.yml # Multi-arch build job (added Jul 2026)
└── AGENTS.md                # THIS FILE
```

## 5. Database Schema

### `users`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | auto |
| email | VARCHAR(255) UNIQUE | login |
| password | VARCHAR(255) | BCrypt |
| first_name | VARCHAR(100) | |
| last_name | VARCHAR(100) | |
| role | VARCHAR(20) | ADMIN / USER |
| refresh_token | TEXT | nullable, JWT |
| created_at | TIMESTAMP | |

### `categories`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR(100) | |
| slug | VARCHAR(100) UNIQUE | |
| description | TEXT | |
| image_url | VARCHAR(500) | nullable |

### `products`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR(255) | |
| slug | VARCHAR(255) UNIQUE | |
| description | TEXT | |
| price | DECIMAL(10,2) | |
| condition | VARCHAR(20) | NEUF, BON_ETAT, RECONDITIONNE |
| console_type | VARCHAR(30) | 25 values in ConsoleType enum |
| stock_quantity | INT | |
| image_url | VARCHAR(500) | nullable, picsum fallback via @PrePersist |
| archived | BOOLEAN | false by default |
| category_id | BIGINT FK → categories | |
| created_at | TIMESTAMP | |

### `carts`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| user_id | BIGINT FK UNIQUE | One user = one cart |

### `cart_items`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| cart_id | BIGINT FK | |
| product_id | BIGINT FK | |
| quantity | INT | |
| UNIQUE(cart_id, product_id) | | |

### `orders`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| user_id | BIGINT FK | |
| status | VARCHAR(20) | PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED |
| total_amount | DECIMAL(10,2) | |
| transaction_id | VARCHAR(100) | nullable UUID |
| created_at | TIMESTAMP | |

### `order_items`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| order_id | BIGINT FK | |
| product_id | BIGINT FK | |
| quantity | INT | |
| unit_price | DECIMAL(10,2) | snapshot at order time |

## 6. API Endpoints

### Auth (`/api/auth`)
| Method | Path | Auth | Body/Params |
|--------|------|------|------------|
| POST | /register | No | {email, password, firstName, lastName} → 409 if exists |
| POST | /login | No | {email, password} → {token, refreshToken, email, role} |
| POST | /refresh | No | {refreshToken} → new token pair |
| GET | /me | JWT | → current user info |

### Products (`/api/products`)
| Method | Path | Auth | Params |
|--------|------|------|--------|
| GET | / | No | page, size, sort, categorySlug, consoleType, condition, minPrice, maxPrice |
| GET | /{slug} | No | → product detail with category |

### Categories (`/api/categories`)
| Method | Path | Auth |
|--------|------|------|
| GET | / | No |

### Cart (`/api/cart`)
| Method | Path | Auth | Body |
|--------|------|------|------|
| GET | / | JWT | |
| POST | /items | JWT | {productId, quantity} |
| PUT | /items/{itemId} | JWT | {quantity} |
| DELETE | /items/{itemId} | JWT | |
| DELETE | / | JWT | clear entire cart |

### Orders (`/api/orders`)
| Method | Path | Auth | Body |
|--------|------|------|------|
| POST | / | JWT | → creates from cart, clears cart |
| GET | / | JWT | user's orders |
| GET | /{id} | JWT | ownership check |

### Payment (`/api/payments`)
| Method | Path | Auth | Body |
|--------|------|------|------|
| POST | /pay | JWT | {orderId} → fake payment, decrements stock |

### Admin (`/api/admin`)
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | /stats | ADMIN | order counts, revenue, top products |
| GET/POST | /products | ADMIN | CRUD |
| GET/PUT/DELETE | /products/{id} | ADMIN | |
| GET/POST | /categories | ADMIN | CRUD |
| GET/PUT/DELETE | /categories/{id} | ADMIN | |
| GET | /orders | ADMIN | all orders |
| PUT | /orders/{id}/status | ADMIN | update status |
| GET | /users | ADMIN | paginated user list |

## 7. ConsoleType Enum (25 values)

NES, SNES, NINTENDO_64, GAMECUBE, WII, WII_U, NINTENDO_SWITCH, GAMEBOY, GAMEBOY_COLOR, GAMEBOY_ADVANCE, NINTENDO_DS, NINTENDO_3DS, PLAYSTATION, PS2, PS3, PS4, PS5, PSP, PS_VITA, SEGA_MEGA_DRIVE, SEGA_SATURN, SEGA_DREAMCAST, XBOX, XBOX_360, XBOX_ONE, AUTRE

## 8. Seed Data (DataInitializer.java)

**Location**: `backend/src/main/java/com/replay/config/DataInitializer.java`

**Users** (2):
1. admin@replay.fr / admin123 (Role.ADMIN)
2. user@replay.fr / user123 (Role.USER)

**Categories** (5): Consoles, Jeux, Manettes, Accessoires, Goodies

**Products** (29) — see section 9 for image URLs:

### Consoles (9)
| # | Name | Slug | Price | Condition | Console | Stock |
|---|------|------|-------|-----------|---------|-------|
| 1 | NES Classic Edition | nes-classic-edition | 59.99 | RECONDITIONNE | NES | 15 |
| 2 | Super Nintendo SNES | super-nintendo-snes | 89.99 | BON_ETAT | SNES | 8 |
| 3 | Nintendo 64 | nintendo-64 | 79.99 | BON_ETAT | NINTENDO_64 | 5 |
| 4 | GameCube | gamecube | 69.99 | BON_ETAT | GAMECUBE | 7 |
| 5 | Wii | wii | 49.99 | RECONDITIONNE | WII | 20 |
| 6 | PlayStation 2 Slim | playstation-2-slim | 54.99 | BON_ETAT | PS2 | 12 |
| 7 | PlayStation 3 | playstation-3 | 89.99 | RECONDITIONNE | PS3 | 10 |
| 8 | Game Boy Advance SP | game-boy-advance-sp | 64.99 | BON_ETAT | GAMEBOY_ADVANCE | 6 |
| 9 | Nintendo DS Lite | nintendo-ds-lite | 44.99 | BON_ETAT | NINTENDO_DS | 9 |

### Jeux (9)
| # | Name | Slug | Price | Condition | Console | Stock |
|---|------|------|-------|-----------|---------|-------|
| 10 | Super Mario World (SNES) | super-mario-world-snes | 24.99 | BON_ETAT | SNES | 30 |
| 11 | Zelda Ocarina of Time | zelda-ocarina-of-time | 34.99 | BON_ETAT | NINTENDO_64 | 15 |
| 12 | Mario Kart 64 | mario-kart-64 | 29.99 | BON_ETAT | NINTENDO_64 | 12 |
| 13 | Pokemon Rouge (GB) | pokemon-rouge-game-boy | 39.99 | BON_ETAT | GAMEBOY | 8 |
| 14 | Super Smash Bros Melee (GC) | super-smash-bros-melee | 44.99 | NEUF | GAMECUBE | 10 |
| 15 | Wii Sports | wii-sports | 14.99 | RECONDITIONNE | WII | 50 |
| 16 | Gran Turismo 4 (PS2) | gran-turismo-4-ps2 | 12.99 | BON_ETAT | PS2 | 25 |
| 17 | The Last of Us (PS3) | the-last-of-us-ps3 | 9.99 | RECONDITIONNE | PS3 | 20 |
| 18 | Sonic 2 (MD) | sonic-2-mega-drive | 14.99 | BON_ETAT | SEGA_MEGA_DRIVE | 18 |

### Manettes (5)
| # | Name | Slug | Price | Condition | Console | Stock |
|---|------|------|-------|-----------|---------|-------|
| 19 | Manette NES | manette-nes-officielle | 19.99 | RECONDITIONNE | NES | 25 |
| 20 | Manette SNES | manette-snes-officielle | 24.99 | BON_ETAT | SNES | 20 |
| 21 | Manette N64 | manette-n64-officielle | 29.99 | BON_ETAT | NINTENDO_64 | 10 |
| 22 | Manette PS2 DualShock 2 | manette-ps2-dualshock-2 | 14.99 | RECONDITIONNE | PS2 | 30 |
| 23 | Manette GameCube | manette-gamecube-officielle | 34.99 | BON_ETAT | GAMECUBE | 8 |

### Accessoires (3)
| # | Name | Slug | Price | Condition | Console | Stock |
|---|------|------|-------|-----------|---------|-------|
| 24 | Carte Memoire PS2 8Mo | carte-memoire-ps2-8mo | 9.99 | NEUF | PS2 | 40 |
| 25 | Adaptateur HDMI Retro | adaptateur-hdmi-retro | 19.99 | NEUF | AUTRE | 35 |
| 26 | Chargeur Console Portable | chargeur-console-portable | 12.99 | NEUF | AUTRE | 45 |

### Goodies (3)
| # | Name | Slug | Price | Condition | Console | Stock |
|---|------|------|-------|-----------|---------|-------|
| 27 | T-Shirt Retro Gamer | t-shirt-retro-gamer | 24.99 | NEUF | AUTRE | 50 |
| 28 | Poster Carte du Monde Retro Gaming | poster-carte-monde-retro | 14.99 | NEUF | AUTRE | 30 |
| 29 | Mug Pixel Heart | mug-pixel-heart | 9.99 | NEUF | AUTRE | 60 |

## 9. Product Image URLs

### Confirmed — Wikimedia Commons (Evan-Amos public domain)

**Consoles:**
1. `https://upload.wikimedia.org/wikipedia/commons/8/82/Nintendo-Entertainment-System-NES-Console-FL.jpg`
2. `https://upload.wikimedia.org/wikipedia/commons/3/36/SNES-Mod1-Console-Set.png`
3. `https://upload.wikimedia.org/wikipedia/commons/b/be/N64-Console-Set.png`
4. `https://upload.wikimedia.org/wikipedia/commons/4/46/GameCube-Console-Set.png`
5. `https://upload.wikimedia.org/wikipedia/commons/f/f3/Wii-Console.png`
6. `https://upload.wikimedia.org/wikipedia/commons/4/4f/PS2-Slim-Console-Set.png`
7. `https://upload.wikimedia.org/wikipedia/commons/5/5e/PS3-Consoles-Set.jpg`
8. `https://upload.wikimedia.org/wikipedia/commons/0/09/Game-Boy-Advance-SP-Mario-Left.png`
9. `https://upload.wikimedia.org/wikipedia/commons/4/4b/Nintendo-DS-Lite-Open.png`

**Controllers:**
10. `https://upload.wikimedia.org/wikipedia/commons/4/4a/NES_controller.JPG`
11. `https://upload.wikimedia.org/wikipedia/commons/3/31/SNES-Controller-in-Hand.jpg`
12. `https://upload.wikimedia.org/wikipedia/commons/5/53/Nintendo-64-Controller-Gray.jpg`
13. `https://upload.wikimedia.org/wikipedia/commons/8/8e/PS2-DualShock2-Controller.jpg`
14. `https://upload.wikimedia.org/wikipedia/commons/a/a5/GameCube_controller.png`

**Accessory:**
15. `https://upload.wikimedia.org/wikipedia/commons/f/f9/Sony-PS2-Memory-Card.jpg`

### Confirmed — Wikipedia (fair-use box art)

16. `https://upload.wikimedia.org/wikipedia/en/3/32/Super_Mario_World_Coverart.png`
17. `https://upload.wikimedia.org/wikipedia/en/5/5d/The_Legend_of_Zelda-_Ocarina_of_Time_box_art.jpg`
18. `https://upload.wikimedia.org/wikipedia/en/9/9a/Mario_Kart_64_box.png`
19. `https://upload.wikimedia.org/wikipedia/en/4/46/Pok%C3%A9mon_Red_Version_box_art.jpg`
20. `https://upload.wikimedia.org/wikipedia/en/0/0b/Super_Smash_Bros_Melee_box_art.png`
21. `https://upload.wikimedia.org/wikipedia/en/1/1a/Wii_Sports_European_box_art.jpg`
22. `https://upload.wikimedia.org/wikipedia/en/7/71/Gran_Turismo_4_box_art.jpg`
23. `https://upload.wikimedia.org/wikipedia/en/4/46/Video_Game_Cover_-_The_Last_of_Us.jpg`
24. `https://upload.wikimedia.org/wikipedia/en/8/8d/Sonic_the_Hedgehog_2_Box_Art.jpg`

### Picsum fallback (no suitable free image found)

25-29. Adaptateur HDMI, Chargeur, T-Shirt, Poster, Mug → picsum (via @PrePersist default)

## 10. Current State

### Done
- All 41 implementation tasks across 7 phases
- 33 commits on main
- 36/36 backend tests passing
- Backend Docker image builds OK
- Frontend builds via `npm start` works
- 24 product images configured (Wikimedia/Wikipedia), 5 fallback picsum via @PrePersist
- package-lock.json regenerated (chokidar mismatch resolved)
- CI/CD workflow: `.github/workflows/ci.yml` with multi-arch build (amd64 + arm64)
- Empty `user/` package removed (User entity lives in `auth/`)

### Notes
- Spring Boot version: 3.4.4 (AGENTS.md updated to match pom.xml)
- `AuthService` physically located in `core/` not `auth/` (all imports correct)
- No local Java/Node/Maven: all builds via Docker containers on Windows

### Next Steps
1. Push to GitHub → CI builds and pushes multi-arch images to GHCR
2. On Oracle Cloud VM: `docker compose -f docker-compose.prod.yml up -d`
3. Set up Nginx reverse proxy with Let's Encrypt SSL (see `deploy/deploy.md`)
4. Update JWT secrets in production environment

## 11. Git Conventions

- Conventional commits: `feat:`, `fix:`, `chore:`, `test:`, `ci:`, `docs:`
- One commit per logical change (not per file)
- Commit messages in English
- Branch: work on `main` (solo project)

## 12. Docker Notes

```yaml
# docker-compose.prod.yml
services:
  postgres:    # PostgreSQL 16, port 5432
  backend:     # Build from backend/Dockerfile, port 8080, depends on postgres
  frontend:    # Build from frontend/Dockerfile, port 80, nginx serving SPA
```

Backend Dockerfile: multi-stage (maven:3.9-eclipse-temurin-17 → eclipse-temurin:17-jre)
Frontend Dockerfile: multi-stage (node:20-alpine → nginx:alpine)

## 13. Deploy (Oracle Cloud)

See `deploy/deploy.md` for full guide. VM Ampere A1:
- `docker compose -f docker-compose.prod.yml up -d`
- Nginx reverse proxy with Let's Encrypt SSL
- GitHub Actions multi-arch build pushed to GitHub Container Registry (ghcr.io)

## 14. ProductCondition Enum

```java
NEUF        // New / never used
BON_ETAT    // Good condition, minor wear
RECONDITIONNE // Refurbished, fully tested
```

## 15. OrderStatus Enum

```java
PENDING    → CONFIRMED → SHIPPED → DELIVERED
PENDING    → CANCELLED
```
