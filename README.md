# RePlay

Plateforme e-commerce retro gaming. Stack : Spring Boot 3.4 + Angular 18 + PostgreSQL 16.

## Stack

- **Backend** : Java 17, Spring Boot 3.4, Spring Security JWT, Spring Data JPA, PostgreSQL 16/H2, Lombok, OpenAPI
- **Frontend** : Angular 18, Tailwind CSS, TypeScript
- **Infra** : Docker Compose, GitHub Actions (CI multi-arch amd64 + arm64)

## Fonctionnalites

- Authentification JWT (access 15min + refresh 7 jours, stocke en base)
- Catalogue produits avec filtres (console, categorie, etat, prix) et pagination
- Panier persistee par utilisateur avec validation stock
- Creation commande avec snapshot prix, paiement factice (decompte stock)
- Administration : dashboard stats, CRUD produits/categories, gestion commandes, liste utilisateurs
- Swagger UI : /swagger-ui.html
- Seed automatique : 2 users, 5 categories, 29 produits (via DataInitializer)

## Demarrage rapide

```bash
docker compose up -d
```

- Backend : http://localhost:8080
- Frontend : http://localhost:4200
- pgAdmin : http://localhost:5050 (admin@replay.fr / admin)
- Swagger : http://localhost:8080/swagger-ui.html

### Comptes de demo (seed automatique)

| Role  | Email            | Mot de passe |
|-------|------------------|--------------|
| Admin | admin@replay.fr  | admin123     |
| User  | user@replay.fr   | user123      |

## Structure du projet

```
backend/          # Spring Boot Maven
  src/main/java/com/replay/
    admin/        # Controleurs admin
    auth/         # Auth (User, JWT, Security)
    cart/         # Panier
    common/       # Exceptions
    config/       # Config (Security, JWT, OpenAPI, DataInitializer)
    order/        # Commandes
    payment/      # Paiement factice
    product/      # Catalogue
frontend/         # Angular
  src/app/
    admin/        # Pages admin
    auth/         # Login, Register
    cart/         # Page panier
    checkout/     # Page commande
    core/         # AuthService, guards, interceptor
    order/        # Historique commandes
    products/     # Catalogue + detail
```

## Commandes

```bash
# Backend (tests)
cd backend && ./mvnw verify

# Frontend
cd frontend && npm ci && npm run build

# Production
docker compose -f docker-compose.prod.yml up -d
```

## API Principale

| Endpoint                | Auth   | Description |
|-------------------------|--------|-------------|
| POST /api/auth/register | -      | Inscription |
| POST /api/auth/login    | -      | Connexion   |
| GET /api/products       | -      | Catalogue   |
| GET /api/products/{slug}| -      | Detail      |
| GET /api/cart           | Bearer | Panier      |
| POST /api/orders        | Bearer | Creer commande |
| POST /api/payments/pay  | Bearer | Paiement    |
| GET /api/admin/stats    | Admin  | Statistiques|

## Deploiement Oracle Cloud

Voir [deploy/deploy.md](deploy/deploy.md).
