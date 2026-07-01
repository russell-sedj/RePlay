# Deploiement RePlay sur Oracle Cloud Free Tier

## 1. Creer une VM Ampere A1 (ARM64)

1. Connectez-vous a Oracle Cloud Console
2. Compute > Instances > Create instance
3. Choisir :
   - Image : **Ubuntu 22.04 Minimal**
   - Shape : **VM.Standard.A1.Flex** (4 OCPU, 24 GB RAM gratuits)
   - SSH keys : ajoutez votre cle publique
4. Ouvrir les ports 80 (HTTP) et 443 (HTTPS) dans le Security List

## 2. Connexion et installation

```bash
ssh ubuntu@<IP_PUBLIQUE>

# Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker

# Docker Compose plugin
sudo apt-get update && sudo apt-get install -y docker-compose-plugin
```

## 3. Deployer l'application

```bash
git clone https://github.com/<votre-user>/replay.git
cd replay

# Demarrer
docker compose -f docker-compose.prod.yml up -d

# Verifier
docker compose ps
```

## 4. Variables d'environnement

Modifier `docker-compose.prod.yml` pour changer les secrets JWT :

```yaml
environment:
  JWT_ACCESS_SECRET: "<votre-secret-256bits>"
  JWT_REFRESH_SECRET: "<votre-secret-256bits>"
```

## 5. Mise a jour

```bash
cd replay
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

## 6. Acces

- Frontend : `http://<IP_PUBLIQUE>`
- Swagger : `http://<IP_PUBLIQUE>:8080/swagger-ui.html`

## Architecture

```
Client -> Nginx (frontend:80) -> Spring Boot (backend:8080) -> PostgreSQL (postgres:5432)
```
