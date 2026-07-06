Chaque tache se termine par un commit Git avec un message conventionnel.
Le message de commit suggere est indique apres chaque tache.

## 1. Initialisation du projet

- [x] 1.1 Initialiser le backend Spring Boot avec Maven (Java 17, Spring Boot 3.4, dependances : spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, postgresql, h2, springdoc-openapi, lombok)
  Commit: `chore: initialize spring boot project with dependencies`

- [x] 1.2 Initialiser le frontend Angular 18 avec Angular CLI, installer et configurer Tailwind CSS, configurer le proxy pour le backend (proxy.conf.json -> http://localhost:8080)
  Commit: `chore: initialize angular project with tailwind`

- [x] 1.3 Creer docker-compose.yml a la racine avec service PostgreSQL 16 (ports 5432:5432, volume nomme pour la persistence des donnees). Creer application-dev.yml pour le profil dev avec les creds PostgreSQL Docker
  Commit: `feat: add docker compose with postgresql`

- [x] 1.4 Creer le workflow GitHub Actions .github/workflows/ci.yml : job backend (ubuntu-latest, Java 17 Temurin, cache Maven, mvn verify) et job frontend (ubuntu-latest, Node 20, cache npm, npm ci, npm run build) en parallele
  Commit: `ci: add github actions workflow for backend and frontend`

## 2. Authentification

- [x] 2.1 Creer l'enum Role (USER, ADMIN) et l'entite User (id, email unique, password, firstName, lastName, role, refreshToken nullable, createdAt). Creer UserRepository avec findByEmail
  Commit: `feat: add user entity with role enum`

- [x] 2.2 Implementer JwtService (generateAccessToken 15min, generateRefreshToken 7 jours, extractEmail, isTokenValid) avec io.jsonwebtoken. Implementer JwtAuthFilter (OncePerRequestFilter) pour extraire le token du header Authorization et setter le SecurityContext
  Commit: `feat: implement jwt service with access and refresh tokens`

- [x] 2.3 Implementer AuthService : register (password 8+ chars, BCrypt, role USER), login (validation credentials, generation tokens, save refreshToken en base), refresh (verification, invalidation ancien token, generation nouveaux tokens)
  Commit: `feat: add authentication service with bcrypt`

- [x] 2.4 Creer AuthController (POST /api/auth/register, POST /api/auth/login, POST /api/auth/refresh, GET /api/auth/me) avec DTOs AuthRequest, RegisterRequest, AuthResponse, UserProfileDTO. Configurer SecurityFilterChain (stateless, CORS localhost:4200, CSRF disabled, /api/auth/** permitAll, /api/admin/** ADMIN, reste authentifie)
  Commit: `feat: add auth endpoints and security configuration`

- [x] 2.5 Creer les pages Angular login et register (ReactiveForms, validation email/password, appel AuthService, stockage tokens dans localStorage via AuthService, redirection vers /products apres login). Creer AuthService, AuthInterceptor (ajoute Authorization: Bearer), AuthGuard
  Commit: `feat: add login and register pages with auth interceptor`

- [x] 2.6 Ecrire les tests JUnit + Mockito pour AuthService (register success, register email existant -> 409, login success, login mauvais mot de passe -> 401, refresh avec token valide, refresh avec token invalide -> 401)
  Commit: `test: add auth service unit tests`

## 3. Catalogue

- [x] 3.1 Creer les enums ProductCondition (NEUF, BON_ETAT, RECONDITIONNE) et ConsoleType (25+ valeurs : NES a AUTRE selon spec). Creer Category (id, name, slug unique, description, imageUrl), Product (id, name, slug unique, description, price BigDecimal, condition, consoleType, stockQuantity, imageUrl, archived boolean, createdAt, category ManyToOne). Creer CategoryRepository et ProductRepository
  Commit: `feat: add product and category entities with enums`

- [x] 3.2 Creer ProductService avec getProducts (Pageable + Specification pour filtres category/consoleType/condition/minPrice/maxPrice, archived=false par defaut), getProductBySlug, getCategories. Utiliser Spring Data JPA Specifications
  Commit: `feat: add product service with filter specifications`

- [x] 3.3 Creer ProductController (GET /api/products avec params page, size, sort, category, console, condition, minPrice, maxPrice, GET /api/products/{slug}, GET /api/categories). Creer ProductSummaryDTO, ProductDetailDTO, CategoryDTO
  Commit: `feat: add catalog endpoints with pagination and filters`

- [x] 3.4 Creer la page Angular catalogue avec grille de produits responsive (Tailwind grid), barre de filtres laterale (console, categorie, condition, prix min/max), pagination en bas, dropdown de tri (prix, nom). Creer ProductService et CategoryService Angular
  Commit: `feat: add product catalog with filters and pagination`

- [x] 3.5 Creer la page detail produit Angular (image, nom, prix, condition badge, console, description, stock, selecteur quantite, bouton ajouter au panier). Route /products/:slug
  Commit: `feat: add product detail page`

- [x] 3.6 Ecrire les tests JUnit + Mockito pour ProductService (pagination renvoie page 0, filtre par console renvoie bons produits, filtre par prix renvoie intervalle, slug inexistant -> exception)
  Commit: `test: add product service unit tests`

## 4. Panier

- [x] 4.1 Creer Cart (id, user OneToOne, createdAt) et CartItem (id, cart ManyToOne, product ManyToOne, quantity int). Unicite sur (cart, product) dans CartItem. Creer CartRepository et CartItemRepository
  Commit: `feat: add cart and cart item entities`

- [x] 4.2 Implementer CartService : getOrCreateCart (si pas de panier, en creer un), addItem (si produit deja present, incremente quantity sinon cree un nouveau CartItem, verifie stock sans decrementer), updateItemQuantity, removeItem, clearCart
  Commit: `feat: implement cart service with stock validation`

- [x] 4.3 Creer CartController (GET /api/cart, POST /api/cart/items, PUT /api/cart/items/{itemId}, DELETE /api/cart/items/{itemId}, DELETE /api/cart). Creer CartDTO, CartItemDTO
  Commit: `feat: add cart endpoints`

- [x] 4.4 Creer la page panier Angular : liste des articles (image, nom, prix unitaire, quantite modifiable avec +/- et input, supprimer bouton, total ligne, total global). Navbar avec compteur de panier (nombre d'articles). Route /cart
  Commit: `feat: add cart page with quantity controls`

- [x] 4.5 Ecrire les tests JUnit + Mockito pour CartService (ajout cree Cart si inexistant, ajout produit deja present incremente, suppression item, stock insuffisant -> exception, clear vide le panier)
  Commit: `test: add cart service unit tests`

## 5. Commandes et paiement factice

- [x] 5.1 Creer OrderStatus enum (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED). Creer Order (id, user ManyToOne, orderDate, status, totalAmount, transactionId nullable, shippingAddress) et OrderItem (id, order ManyToOne, product ManyToOne, quantity, unitPrice)
  Commit: `feat: add order and order item entities`

- [x] 5.2 Implementer OrderService : createOrder (copie CartItems -> OrderItems avec snapshot unitPrice, calcule totalAmount, vide le panier, status PENDING), getOrdersByUser (pageable, orderDate DESC), getOrderById
  Commit: `feat: implement order creation from cart`

- [x] 5.3 Implementer PaymentService : pay(orderId, method) — verifie que la commande appartient au user, verifie status PENDING, verifie stock pour chaque OrderItem, decremente stock, genere transactionId "TXN-<UUID>", passe status CONFIRMED. Creer PaymentController (POST /api/payments/pay { orderId, method })
  Commit: `feat: add fake payment with stock decrement`

- [x] 5.4 Creer OrderController (POST /api/orders, GET /api/orders, GET /api/orders/{id}). Creer OrderSummaryDTO, OrderDetailDTO, PaymentRequest, PaymentResponse
  Commit: `feat: add order endpoints`

- [x] 5.5 Creer la page checkout Angular : recapitulatif commande (liste articles, quantites, prix, total), formulaire adresse livraison, bouton Payer qui appelle le paiement factice, ecran confirmation (succes avec transactionId + orderId). Routes /checkout et /orders
  Commit: `feat: add checkout page with fake payment confirmation`

- [x] 5.6 Creer la page historique commandes Angular : liste paginee des commandes (id, date, statut badge colore, total, nb articles). Page detail commande (tous les items avec prix snapshot, statut, adresse, transactionId si CONFIRMED). Route /orders et /orders/:id
  Commit: `feat: add order history and detail pages`

- [x] 5.7 Ecrire les tests JUnit + Mockito pour OrderService (creation depuis panier, panier vide -> erreur, snapshot prix correct) et PaymentService (paiement OK -> stock decremente + status CONFIRMED, stock insuffisant -> erreur, commande deja payee -> erreur, commande autre user -> 403)
  Commit: `test: add order and payment service tests`

## 6. Administration

- [x] 6.1 Creer AdminController avec GET /api/admin/stats (totalUsers, totalOrders, totalRevenue somme des totalAmount CONFIRMED, totalProducts non archives, topProducts top 5 par quantite vendue). Creer StatsDTO
  Commit: `feat: add admin statistics endpoint`

- [x] 6.2 Ajouter les endpoints admin produits : GET /api/admin/products (inclut archived, memes filtres que public), POST /api/admin/products (creation avec slug genere), PUT /api/admin/products/{id} (mise a jour), PUT /api/admin/products/{id} avec archived=true (soft delete)
  Commit: `feat: add admin product management endpoints`

- [x] 6.3 Ajouter les endpoints admin categories : GET /api/admin/categories, POST /api/admin/categories, PUT /api/admin/categories/{id}, DELETE /api/admin/categories/{id} (bloque si produits associes)
  Commit: `feat: add admin category management endpoints`

- [x] 6.4 Ajouter les endpoints admin commandes : GET /api/admin/orders (pageable, filtrable par status), PUT /api/admin/orders/{id}/status (valider transitions autorisees). Ajouter GET /api/admin/users (pageable, read-only)
  Commit: `feat: add admin order and user management endpoints`

- [x] 6.5 Creer les pages admin Angular (lazy-loaded, AdminGuard) : dashboard avec stats (cards totalUsers, totalOrders, totalRevenue, totalProducts + top 5 produits table), gestion produits (tableau pagine + modal creation/edition avec tous les champs), gestion categories (liste + modal CRUD)
  Commit: `feat: add admin dashboard and product/category pages`

- [x] 6.6 Completer les pages admin : gestion commandes (tableau pagine, filtrable par status, dropdown changement statut avec validation transitions), liste utilisateurs (tableau read-only pagine). Route /admin protegee par AdminGuard
  Commit: `feat: add admin order and user management pages`

- [x] 6.7 Creer DataInitializer (CommandLineRunner) : seed si users vide -> 1 admin (admin@replay.fr / admin123), 5 categories (Consoles, Jeux, Manettes, Accessoires, Goodies), 20+ produits repartis avec differents etats, consoles, prix 5-500 EUR, stocks 1-50. Images via picsum.photos avec seed different par produit
  Commit: `feat: add data initializer with demo products`

## 7. Finition et deploiement

- [x] 7.1 Configurer SpringDoc OpenAPI : titre API RePlay, tag par domaine (Auth, Products, Cart, Orders, Admin), scheme bearer JWT dans Swagger UI. Verifier acces a /swagger-ui.html
  Commit: `docs: add openapi documentation with swagger`

- [x] 7.2 Ecrire les tests Angular Jasmine/Karma : AuthService (login, register, refresh, stockage/recuperation tokens), CartService (addItem, updateQuantity, removeItem, clearCart), ProductService (getProducts avec filtres)
  Commit: `test: add angular service unit tests`

- [x] 7.3 Creer le README.md : description RePlay, stack, captures d'ecran (placeholder), instructions docker compose up, structure projet, fonctionnalites, endpoint API principaux
  Commit: `docs: add project readme`

- [x] 7.4 Creer backend/Dockerfile multi-stage : stage 1 maven:3.9-eclipse-temurin-17 build JAR, stage 2 eclipse-temurin:17-jre copy JAR, EXPOSE 8080, ENTRYPOINT java -jar. Creer frontend/Dockerfile multi-stage : stage 1 node:20-alpine build Angular, stage 2 nginx:alpine copy dist, nginx.conf avec try_files $uri /index.html
  Commit: `feat: add production dockerfiles`

- [x] 7.5 Creer docker-compose.prod.yml : services postgres, backend (image replay-backend, depends_on postgres, port 8080, env DB), frontend (image replay-frontend, port 80). Integrer docker buildx multi-arch dans le workflow CI pour push vers registry
  Commit: `feat: add production docker compose and multi-arch ci build`

- [x] 7.6 Creer deploy/deploy.md avec instructions Oracle Cloud : creation VM Ampere A1 Ubuntu 22.04, installation Docker/Compose, clonage repo, variable env, docker compose -f docker-compose.prod.yml up -d, verification acces IP publique
  Commit: `docs: add oracle cloud deployment guide`
