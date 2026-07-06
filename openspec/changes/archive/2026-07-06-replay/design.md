## Context

RePlay est une plateforme e-commerce de gaming d'occasion et reconditionne developpee comme projet full stack Java/Spring Boot + Angular 18. Le projet suit une architecture monolithique avec deux applications distinctes (backend REST API, frontend SPA) communiquant via HTTP/JSON. Les specs sont ecrites pour etre executees par un agent IA (DeepSeek, Claude) via opsx-apply, avec des instructions suffisamment detaillees pour eviter toute ambiguite.

Le deploiement cible Oracle Cloud Free Tier (Ampere A1 ARM64) impose une contrainte de compatibilite multi-arch pour les images Docker. Le paiement est simule cote serveur (pas de Stripe, pas de cle API externe) pour rester zero budget. Les images produits utilisent des placeholders picsum.photos.

## Goals / Non-Goals

**Goals:**
- Backend Spring Boot 3.4 avec API REST securisee par JWT (access + refresh tokens)
- Frontend Angular 18 SPA avec Tailwind CSS, routes publiques et protegees
- Authentification avec roles USER et ADMIN
- Catalogue produit avec pagination, filtres multi-criteres et tri
- Panier persistant par utilisateur
- Paiement factice et gestion de stock a la confirmation
- Admin integre dans la meme SPA (lazy-loaded routes)
- Conteneurisation avec Docker Compose
- CI/CD GitHub Actions avec build multi-arch (AMD64 + ARM64)
- Deploiement Oracle Cloud Free Tier

**Non-Goals:**
- Paiement reel (Stripe, PayPal) — le code est structure comme un processeur de paiement pour faciliter une migration future
- Upload d'images — placeholders picsum.photos uniquement
- Panier visiteur non connecte — consultation seulement sans auth
- Notifications email, newsletter
- Chat ou support client
- Multi-langue
- Tests end-to-end (Playwright/Cypress)
- multi-sessions (un utilisateur = un refresh token)

## Decisions

### 1. JWT avec paire access/refresh tokens

**Decision:** Access token (15 minutes, envoye via Authorization: Bearer) + refresh token (7 jours, stocke en base et en localStorage).

**Rationale:** Le refresh token evite les reconnexions frequentes. Le stockage en base permet l'invalidation cote serveur (deconnexion, changement de mot de passe). L'access token court limite l'impact en cas de vol. Le refresh token en localStorage est pragmatique pour ce projet.

**Alternatives considerees:**
- Cookie HTTP-only pour le refresh token : plus securise mais necessite CSRF, configuration plus complexe. Non justifie pour un projet de demonstration.
- OAuth2/Keycloak : ajoute un conteneur, complexite hors de proportion.

### 2. Admin integre dans la meme SPA

**Decision:** Les routes admin sont des routes Angular lazy-loadees protegees par un guard verifiant le role ADMIN.

**Rationale:** Une seule application a maintenir, deployer et surveiller. Le lazy loading empeche tout chargement des modules admin par les utilisateurs non-admin. Le guard Angular empeche l'acces cote front, le backend verifie le role sur chaque endpoint admin.

**Alternatives considerees:**
- Application Angular separee : maintenance double, authentification a partager. Inutile.

### 3. Paiement factice cote serveur

**Decision:** Un endpoint backend simule le paiement : valide le panier, verifie le stock, decremente, genere un transactionId (TXN-<UUID>), confirme la commande.

**Rationale:** Zero dependance externe, zero cout, zero cle API. Le flux architectural est identique a un vrai paiement : appel API, validation, mise a jour de stock, confirmation de commande. Le code est structure pour faciliter l'integration future d'un veritable processeur (Stripe, PayPal) sans changer l'architecture.

**Alternatives considerees:**
- Stripe sandbox : necessite compte Stripe, gestion des webhooks, cles API. Complexite sans valeur ajoutee.
- Paiement cote front uniquement : ne demontre pas l'architecture backend.

### 4. Decrement du stock a la confirmation de commande

**Decision:** Le stock est decremente au moment du paiement (confirmation de commande), pas a l'ajout au panier.

**Rationale:** Le panier est temporaire. Un produit peut rester dans un panier sans etre achete. Decrementer a l'ajout bloquerait d'autres acheteurs.

**Alternatives considerees:**
- Decrement a l'ajout : bloque le stock pour d'autres utilisateurs.
- Reservation avec expiration : necessite un scheduler/background job. Trop complexe.

### 5. Images Docker multi-arch pour Oracle ARM64

**Decision:** Le CI/CD GitHub Actions utilise docker buildx pour construire des images linux/amd64 et linux/arm64. Le deploiement Oracle Cloud utilise uniquement les images ARM64.

**Rationale:** Oracle Cloud Free Tier fournit des instances Ampere A1 (ARM64). Les runners GitHub Actions gratuits sont x86_64. La solution multi-arch permet de builder depuis GitHub Actions sans runner self-hosted sur Oracle.

**Alternatives considerees:**
- Runner self-hosted sur Oracle : build natif plus rapide mais setup complexe.
- Build x86 uniquement + QEMU emulation : performances degradees.

### 6. Refresh token en base dans la table users

**Decision:** Le refresh token est stocke dans une colonne refresh_token de la table users, mise a jour a chaque refresh.

**Rationale:** Simple, permet l'invalidation cote serveur. Un admin peut deconnecter un utilisateur en supprimant son refresh token. Pas de table supplementaire.

**Alternatives considerees:**
- Table refresh_tokens separee : permettrait sessions multiples. Inutile pour ce projet (une session par user).

### 7. Backend monolithique modulaire

**Decision:** Le backend est organise en packages par domaine fonctionnel (auth, product, cart, order, payment, user, admin) avec des services et controllers dedies.

**Rationale:** Organisation claire sans la complexite des microservices. Chaque package contient ses entites, repository, service, DTO et controller. Les packages sont independants (pas de dependances cycliques).

**Alternatives considerees:**
- Microservices : surcharge operationnelle pour un projet de cette taille. Complexite Docker, communication inter-services.

## Risks / Trade-offs

- **Refresh token en localStorage** : vulnerable au XSS. Si compromis, l'attaquant peut rafraichir l'access token indefiniment. Acceptable pour un projet de demonstration. L'access token expire en 15 minutes.
- **Paiement factice** : ne demontre pas d'integration reelle. Le code est structure pour une migration facile.
- **Build multi-arch** : double le temps de build sur GitHub Actions. Acceptable vu la taille du projet.
- **Pas de lock optimiste/concurrent** : si deux utilisateurs commandent le dernier exemplaire simultanement, seul le premier est traite (stock negatif evite par verification pre-decrement).
- **Pas de cache** : les requetes catalogue sont touchees en base a chaque appel. Acceptable pour un MVP sans fort trafic.