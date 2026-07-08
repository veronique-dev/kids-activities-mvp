# Comptes et configuration

## Comptes de démonstration

Créés au démarrage (seed data) :

| Rôle | E-mail | Mot de passe | Usage |
|------|--------|--------------|-------|
| **ADMIN** | admin@kidsactivities.fr | admin123 | Dashboard admin, CRUD activités |
| **PARENT** | parent@example.com | parent123 | Réservations, profil |

## Ports (Docker)

| Service | Port hôte |
|---------|-----------|
| Frontend | 5173 |
| API Gateway | 8080 |
| Auth service | 8081 (interne) |
| Activity service | 8082 (interne) |
| Booking service | 8083 (interne) |
| Notification service | 8084 (interne) |
| PostgreSQL | **5433** → 5432 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management | 15672 |
| Mailpit UI | 8025 |
| Mailpit SMTP | 1025 |

## Bases de données

| Base | Service |
|------|---------|
| auth_db | auth-service |
| activity_db | activity-service |
| booking_db | booking-service |

**Connexion depuis l'hôte :**

```
Host: localhost
Port: 5433
User: kids
Password: kids123
```

## Variables d'environnement clés

| Variable | Valeur dev | Description |
|----------|------------|-------------|
| `JWT_SECRET` | (voir docker-compose) | Secret signature JWT — **à changer en prod** |
| `INTERNAL_API_KEY` | internal-dev-key | Appels inter-services |
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://postgres:5432/... | URL BDD par service |
| `RABBITMQ_HOST` | rabbitmq | Broker messages |
| `MAIL_FROM` | noreply@kidsactivities.fr | Expéditeur e-mails |
| `MAIL_ADMIN` | admin@kidsactivities.fr | Destinataire copies admin |

## JWT

- Header : `Authorization: Bearer <token>`
- Obtenu via `POST /api/auth/login` ou `POST /api/auth/register`
- Contient le claim `role` : `PARENT` ou `ADMIN`
- Expiration configurable dans auth-service (`application.yml`)

## Mailpit (développement)

Tous les e-mails sont capturés localement :

- Interface web : http://localhost:8025
- Aucun e-mail réel n'est envoyé en dev

## Frontend

- Token stocké dans `localStorage` (clé utilisée par le client API)
- En cas de 401, le token est automatiquement supprimé
