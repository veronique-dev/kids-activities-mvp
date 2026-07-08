# Architecture

## Vue d'ensemble

Le MVP est découpé en **microservices** Spring Boot 3.3, orchestrés par Docker Compose. Le frontend React communique uniquement avec l'**API Gateway**.

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────────────────────────┐
│  Frontend   │────▶│   Gateway    │────▶│ auth-service      :8081  auth_db   │
│  :5173      │     │   :8080      │     │ activity-service  :8082  activity_db│
└─────────────┘     └──────────────┘     │ booking-service   :8083  booking_db │
                              │            └─────────────────────────────────────┘
                              │                          │
                              │            ┌─────────────▼─────────────┐
                              └───────────▶│ notification-service :8084 │
                                           │ (consomme RabbitMQ)       │
                                           └─────────────┬─────────────┘
                                                         │
                              ┌──────────────┐    ┌──────▼──────┐
                              │  RabbitMQ    │◀───│   Mailpit   │
                              │  :5672       │    │   :8025     │
                              └──────────────┘    └─────────────┘
```

## Modules Maven

| Module | Rôle |
|--------|------|
| `common/` | DTOs, événements RabbitMQ, exceptions partagées |
| `services/auth-service/` | Inscription, connexion, JWT, profil utilisateur |
| `services/activity-service/` | CRUD activités, gestion des places |
| `services/booking-service/` | Réservations, annulations, saga (réserve/libère via HTTP) |
| `services/notification-service/` | E-mails async (événements RabbitMQ → Mailpit/SMTP) |
| `services/gateway/` | Spring Cloud Gateway + BFF admin (`/api/admin/**`) |
| `e2e-tests/` | Tests d'acceptation REST Assured via gateway |
| `backend/` | Monolithe legacy (conservé, non utilisé par Docker) |

## Routage Gateway

| Préfixe | Service cible |
|---------|---------------|
| `/api/auth/**` | auth-service |
| `/api/users/**` | auth-service |
| `/api/activities/**` | activity-service |
| `/api/bookings/**` | booking-service |
| `/api/admin/**` | gateway (agrégation BFF) |

## Événements asynchrones (RabbitMQ)

| Événement | Émetteur | Consommateur |
|-----------|----------|--------------|
| `UserRegisteredEvent` | auth-service | notification-service → e-mail bienvenue |
| `BookingConfirmedEvent` | booking-service | notification-service → e-mail confirmation |
| `BookingCancelledEvent` | booking-service | notification-service → e-mail annulation |

## Bases de données

PostgreSQL héberge trois bases isolées :

- `auth_db` — utilisateurs
- `activity_db` — activités
- `booking_db` — réservations

Script d'initialisation : `infra/postgres/init-databases.sql`

## Sécurité

- Authentification **JWT** (header `Authorization: Bearer <token>`)
- Rôle dans le token : `PARENT` ou `ADMIN`
- Routes admin protégées par `@PreAuthorize("hasRole('ADMIN')")`
- Appels inter-services : header `X-Internal-Api-Key`

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Backend | Java 21, Spring Boot 3.3.6, Spring Cloud Gateway |
| Frontend | React, Vite, nginx (prod Docker) |
| BDD | PostgreSQL 16 |
| Messaging | RabbitMQ 3 |
| E-mails (dev) | Mailpit |
| Tests E2E | REST Assured, JUnit 5 |
