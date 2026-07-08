# Démarrage rapide

## Prérequis

- **Docker** et **Docker Compose** (mode recommandé)
- Ou en local : Java 21, Maven 3.9+, Node.js 20+, PostgreSQL 16

## Mode Docker (recommandé)

À la racine du projet :

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Mailpit (e-mails) | http://localhost:8025 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| PostgreSQL (hôte) | localhost:**5433** (user `kids`, password `kids123`) |

> PostgreSQL est mappé sur le port **5433** pour éviter les conflits avec une instance locale sur 5432.

Arrêter la stack :

```bash
docker compose down
```

## Mode local (sans Docker pour le backend)

1. Démarrer PostgreSQL et créer les bases `auth_db`, `activity_db`, `booking_db` (voir `infra/postgres/init-databases.sql`).
2. Démarrer RabbitMQ et Mailpit (ou via Docker pour ces seuls services).
3. Lancer chaque microservice :

```bash
mvn -pl services/auth-service spring-boot:run
mvn -pl services/activity-service spring-boot:run
mvn -pl services/booking-service spring-boot:run
mvn -pl services/notification-service spring-boot:run
mvn -pl services/gateway spring-boot:run
```

4. Frontend :

```bash
cd frontend && npm install && npm run dev
```

Le frontend en dev pointe vers `http://localhost:8080` (gateway).

## Lancer les tests

### Tests unitaires (tous les microservices)

```bash
mvn test -pl services/auth-service,services/activity-service,services/booking-service,services/notification-service
```

### Tests E2E (stack Docker requise)

```bash
mvn -pl e2e-tests -Pe2e test -De2e.base.url=http://localhost:8080
```

### Tests E2E avec vérification Mailpit

```bash
mvn -pl e2e-tests -Pe2e test \
  -De2e.base.url=http://localhost:8080 \
  -De2e.mailpit.enabled=true \
  -De2e.mailpit.url=http://localhost:8025
```

## Comptes de démonstration

| Rôle | E-mail | Mot de passe |
|------|--------|--------------|
| Admin | admin@kidsactivities.fr | admin123 |
| Parent | parent@example.com | parent123 |

Voir [Comptes & configuration](05-comptes-et-configuration.md) pour le détail.
