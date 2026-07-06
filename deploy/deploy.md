# Déploiement RePlay sur Oracle Cloud Free Tier

> Ce guide part du principe que le code est sur GitHub et que le CI push automatiquement les images multi-arch (amd64 + arm64) vers **ghcr.io** (GitHub Container Registry).

---

## Étape 0 — Avant de commencer

- [ ] Avoir un **compte GitHub** avec le dépôt `replay` pushé
- [ ] Avoir un **compte Oracle Cloud** avec une VM Ampere A1 (toujours dans le Free Tier)
- [ ] Avoir un **nom de domaine** (optionnel mais recommandé pour le SSL)

---

## Étape 1 — Sur GitHub

### 1.1 Push du code

```bash
git remote add origin git@github.com:<TON_USER>/replay.git
git push -u origin main
```

### 1.2 Vérifier le CI

Aller sur **GitHub > Actions > CI — Multi-arch Build** :
- Les jobs `test-backend` et `test-frontend` doivent passer au vert
- Le job `build-and-push` doit builder et push les images vers `ghcr.io/<TON_USER>/backend` et `ghcr.io/<TON_USER>/frontend`

### 1.3 Générer un Personal Access Token (PAT)

Pour qu'Oracle Cloud puisse pull les images privées depuis GHCR :

1. GitHub > Settings > Developer settings > Personal access tokens > Fine-grained tokens
2. Créer un token avec :
   - **Repository access** : Only select repositories → `replay`
   - **Permissions** : Contents (read), Packages (read)
3. Copier le token (ex: `ghp_xxxxxxxx`)

---

## Étape 2 — Créer la VM Oracle Cloud

### 2.1 Dans la console Oracle

1. **Compute > Instances > Create instance**
2. Configurer :
   - **Name** : `replay-vm`
   - **Image** : `Ubuntu 22.04 Minimal` (ou `Canonical Ubuntu 22.04`)
   - **Shape** : `VM.Standard.A1.Flex` (4 OCPU, 24 GB RAM — gratuit)
   - **SSH keys** : ajouter ta clé publique
3. **Réseau** : ouvrir les ports **80** (HTTP), **443** (HTTPS), **22** (SSH)

> Note : la VM A1.Flex met 2-3 minutes à provisionner.

### 2.2 Connexion SSH

```bash
ssh ubuntu@<IP_PUBLIQUE>
```

---

## Étape 3 — Installer Docker sur la VM

```bash
# Docker Engine
curl -fsSL https://get.docker.com | sudo sh

# Ajouter l'utilisateur au groupe docker
sudo usermod -aG docker $USER

# Docker Compose plugin
sudo apt-get update && sudo apt-get install -y docker-compose-plugin

# Redémarrer la session (ou exec: newgrp docker)
exit
# reconnecte-toi
ssh ubuntu@<IP_PUBLIQUE>
```

---

## Étape 4 — Se connecter à GHCR

```bash
echo "<TON_PAT_TOKEN>" | docker login ghcr.io -u <TON_USER> --password-stdin
```

Teste que ça marche :

```bash
docker pull ghcr.io/<TON_USER>/backend:latest
docker pull ghcr.io/<TON_USER>/frontend:latest
```

---

## Étape 5 — Préparer docker-compose.prod.yml sur la VM

Crée un dossier et le fichier compose :

```bash
mkdir -p ~/replay
cd ~/replay
nano docker-compose.prod.yml
```

Contenu à adapter :

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: replay
      POSTGRES_USER: replay
      POSTGRES_PASSWORD: replay
    volumes:
      - pgdata:/var/lib/postgresql/data
    restart: unless-stopped

  backend:
    image: ghcr.io/<TON_USER>/backend:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/replay
      SPRING_DATASOURCE_USERNAME: replay
      SPRING_DATASOURCE_PASSWORD: replay
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      JWT_ACCESS_SECRET: "<GENERER_UN_SECRET_ALEATOIRE>"
      JWT_REFRESH_SECRET: "<GENERER_UN_AUTRE_SECRET_ALEATOIRE>"
    depends_on:
      - postgres
    restart: unless-stopped

  frontend:
    image: ghcr.io/<TON_USER>/frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  pgdata:
```

> ⚠️ **Générer les secrets JWT** :
> ```bash
> openssl rand -base64 32
> ```

### Alternative : pull le `docker-compose.prod.yml` depuis GitHub

```bash
cd ~/replay
wget https://raw.githubusercontent.com/<TON_USER>/replay/main/docker-compose.prod.yml
```

---

## Étape 6 — Lancer l'application

```bash
cd ~/replay
docker compose -f docker-compose.prod.yml up -d

# Vérifier le statut
docker compose ps

# Voir les logs
docker compose logs -f
```

### Tester

```bash
curl http://localhost:80
curl http://localhost:8080/api/products
curl http://localhost:8080/swagger-ui.html
```

---

## Étape 7 — Nom de domaine + SSL (Let's Encrypt)

> Optionnel mais fortement recommandé. Sans ça l'API ne tourne qu'en HTTP.

### 7.1 Configurer le DNS

Ajouter un enregistrement **A** chez ton registrar :
- `replay.fr` → `<IP_PUBLIQUE>`
- `www.replay.fr` → `<IP_PUBLIQUE>`

### 7.2 Installer Nginx comme reverse proxy (sur la VM)

```bash
# Arrêter le frontend sur le port 80
docker compose stop frontend

# Installer Nginx
sudo apt install -y nginx

# Installer Certbot
sudo apt install -y certbot python3-certbot-nginx
```

### 7.3 Configurer Nginx

```bash
sudo nano /etc/nginx/sites-available/replay
```

```nginx
server {
    listen 80;
    server_name replay.fr www.replay.fr;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/replay /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 7.4 Obtenir le certificat SSL

```bash
sudo certbot --nginx -d replay.fr -d www.replay.fr
```

Choix : **2** (redirect HTTP → HTTPS)

Les certificats se renouvellent automatiquement via `certbot renew --dry-run`.

### 7.5 Modifier docker-compose.prod.yml

Remplacer le port `80:80` du frontend par `8081:80` pour que Nginx proxy :

```yaml
  frontend:
    ports:
      - "8081:80"
```

Puis :

```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## Étape 8 — Mettre à jour l'application

Après un push sur `main` :
1. Le CI build et push automatiquement les nouvelles images vers GHCR
2. Sur la VM :

```bash
cd ~/replay
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

### Astuce : script de mise à jour

```bash
#!/bin/bash
# ~/replay/update.sh
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
docker image prune -f
```

```bash
chmod +x ~/replay/update.sh
```

---

## Étape 9 — Backup PostgreSQL

Backup quotidien via cron :

```bash
crontab -e
```

Ajouter :

```
0 3 * * * docker exec replay-postgres-1 pg_dump -U replay replay > ~/backups/replay_$(date +\%Y\%m\%d).sql
```

> ⚠️ Changer `replay-postgres-1` par le vrai nom du conteneur : `docker ps | grep postgres`

---

## Vérification finale

| URL | Ce qu'on doit voir |
|-----|-------------------|
| `https://replay.fr` | Catalogue produits |
| `https://replay.fr/login` | Page de connexion |
| `https://replay.fr/api/products` | JSON des produits |
| `https://replay.fr/api/swagger-ui.html` | Documentation API |

Comptes de test :
- **Admin** : admin@replay.fr / admin123
- **User** : user@replay.fr / user123

---

## Dépannage

| Problème | Solution |
|----------|----------|
| `docker compose` : command not found | `sudo apt-get install -y docker-compose-plugin` |
| GHCR : permission denied | Vérifier le PAT (Permissions : Packages read) |
| Port 80 déjà utilisé | `sudo lsof -i :80`, puis `sudo kill <PID>` |
| Base de données : connection refusée | `docker compose logs postgres` |
| CORS error | Vérifier `localhost:4200` dans le backend, ou le retirer en prod |
| `docker pull` lent | C'est normal au premier pull (~300MB par image). Les suivants sont plus rapides grâce au cache |
