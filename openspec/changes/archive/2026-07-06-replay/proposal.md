## Why

RePlay est une plateforme e-commerce de gaming d'occasion et reconditionne (consoles retro, jeux, manettes, accessoires). Le marche du retrogaming est en pleine expansion mais les plateformes existantes sont soit generalistes (LeBonCoin, eBay), soit uniquement neuf. Il manque une plateforme dediee au gaming d'occasion avec navigation par console et etat clair des produits.

Cote developpement, ce projet comble le gap Java/Spring Boot identifie dans le profil — apres plusieurs projets Python/Data, une application full stack Java + Angular demontre une polyvalence technique recherchee par les ESN. Le choix du gaming comme domaine rend le projet parlant et differenciant du enieme clone Shopify ou Trello.

## What Changes

- Creation d'un backend Spring Boot 3 avec API REST securisee par JWT
- Creation d'un frontend Angular 18 SPA avec Tailwind CSS
- Authentification complete (inscription, connexion, refresh token, roles USER/ADMIN)
- Catalogue de produits gaming avec filtres multi-criteres et pagination
- Panier utilisateur persistant avec CRUD
- Commande avec paiement factice (simulation cote serveur) et gestion de stock
- Espace admin integre avec dashboard statistiques et CRUD
- Conteneurisation Docker Compose avec PostgreSQL
- CI/CD GitHub Actions avec build multi-arch (AMD64 + ARM64)
- Deploiement sur Oracle Cloud Free Tier (Ampere A1)

## Capabilities

### New Capabilities

- `auth`: Inscription, connexion par email/mot de passe, JWT (access token 15min + refresh token 7 jours), roles USER et ADMIN, securite Spring Security, endpoint /me
- `catalog`: Catalogue de produits gaming avec pagination, filtres (console, categorie, etat, prix), tri, categories navigables. Images via placeholders picsum.photos
- `cart`: Panier utilisateur avec ajout/suppression/modification de quantite, verification de stock (sans decrement), vidage complet
- `checkout`: Creation de commande depuis le panier, paiement factice (simulation transaction ID), decrement du stock a la confirmation, historique des commandes
- `admin`: Dashboard statistiques, gestion produits et categories (CRUD), gestion commandes (changement de statut), liste utilisateurs. Routes protegees par role ADMIN
- `ci`: Workflow GitHub Actions avec build multi-arch (docker buildx), tests automatiques, verification lint/compilation
- `deploy`: Docker Compose production, Dockerfiles multi-stage pour backend et frontend, guide deploiement Oracle Cloud ARM64

### Modified Capabilities

None (nouveau projet).

## Impact

- **Dependencies:** Java 17+, Spring Boot 3.4, Spring Security 6, Spring Data JPA, Hibernate, PostgreSQL 16, Angular 18, Tailwind CSS, Docker, GitHub Actions. Toutes open source et gratuites.
- **File system:** Structure Maven standard (backend/) + Angular CLI standard (frontend/), docker-compose.yml racine.
- **Deployment:** Oracle Cloud Free Tier (VM Ampere A1 ARM64, 4 CPU, 24 Go RAM). Paiement factice (pas de Stripe, pas de cle API).
- **Git:** Commits progressifs avec messages conventionnels (feat:, chore:, test:, ci:, docs:). Une tache = un commit.