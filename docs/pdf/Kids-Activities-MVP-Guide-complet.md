# Kids Activities MVP — Guide complet

*Généré le 08/07/2026 à 15:54*

---


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

---


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

---


**Projet :** Kids Activities MVP v1.0.0  
**Source JSON :** `product/user-stories-mvp.json`  
**Export CSV (Jira) :** `product/user-stories-mvp.csv`

## Comptes de test

| Rôle | E-mail | Mot de passe |
|------|--------|--------------|
| Admin | admin@kidsactivities.fr | admin123 |
| Parent | parent@example.com | parent123 |

---

## EPIC-1 — Découverte des activités

### US-01 — Consulter le catalogue d'activités

**Priorité :** Must | **Persona :** Visiteur / Parent

> En tant que visiteur ou parent, je veux voir la liste des activités disponibles, afin de choisir une activité adaptée à mon enfant.

**Critères d'acceptation :**

- Given je suis un visiteur non connecté, When j'accède à /, Then je vois les activités actives sans connexion
- Given des activités existent, When j'accède au catalogue, Then chaque carte affiche titre, description, date, lieu, prix et places restantes
- Given aucune activité active, When j'accède au catalogue, Then le message vide s'affiche

**Test auto :** `MvpAcceptanceTest.us01_publicActivitiesList`

---

### US-02 — Voir le détail d'une activité

**Priorité :** Must | **Persona :** Visiteur / Parent

> En tant que visiteur ou parent, je veux consulter les informations d'une activité, afin de décider si elle convient à mon enfant.

**Critères d'acceptation :**

- Given une activité active, When je consulte GET /api/activities/{id}, Then les champs détaillés sont retournés
- Given 0 place restante, When je suis parent connecté, Then le bouton Réserver est désactivé côté UI

**Test auto :** `MvpAcceptanceTest.us02_activityDetail`

---

## EPIC-2 — Compte et authentification

### US-03 — Créer un compte parent

**Priorité :** Must | **Persona :** Visiteur

> En tant que visiteur, je veux m'inscrire, afin de réserver des activités pour mes enfants.

**Critères d'acceptation :**

- Given un e-mail unique, When POST /api/auth/register, Then compte PARENT créé et token JWT retourné
- Given un e-mail existant, When POST /api/auth/register, Then 400 avec message d'erreur
- Given inscription réussie, When l'événement est publié, Then e-mail de bienvenue envoyé (Mailpit)

**Test auto :** `MvpAcceptanceTest.us03_registerAndDuplicateEmail`

---

### US-04 — Me connecter

**Priorité :** Must | **Persona :** Parent

> En tant que parent inscrit, je veux me connecter, afin d'accéder à mes réservations.

**Critères d'acceptation :**

- Given identifiants valides, When POST /api/auth/login, Then token JWT retourné
- Given mauvais mot de passe, When POST /api/auth/login, Then 401

**Test auto :** `MvpAcceptanceTest.us04_loginValidAndInvalid`

---

### US-05 — Me déconnecter

**Priorité :** Must | **Persona :** Parent

> En tant que parent connecté, je veux me déconnecter, afin de sécuriser mon compte.

**Critères d'acceptation :**

- Given je suis connecté, When je me déconnecte côté UI, Then le token localStorage est supprimé

**Test auto :** manuel (UI)

---

## EPIC-3 — Réservations

### US-07 — Réserver une place pour mon enfant

**Priorité :** Must | **Persona :** Parent

> En tant que parent connecté, je veux réserver une activité pour mon enfant.

**Critères d'acceptation :**

- Given parent connecté et places disponibles, When POST /api/bookings, Then réservation CONFIRMED et places décrémentées
- Given réservation existante, When nouvelle réservation même activité, Then 400
- Given 0 place, When POST /api/bookings, Then 400

**Test auto :** `MvpAcceptanceTest.us07_createBooking`, `MvpAcceptanceTest.us07_duplicateBookingRejected`

---

### US-08 — Consulter mes réservations

**Priorité :** Must | **Persona :** Parent

> En tant que parent connecté, je veux voir mes réservations.

**Critères d'acceptation :**

- Given parent connecté, When GET /api/bookings, Then uniquement ses réservations
- Given non authentifié, When GET /api/bookings, Then 401

**Test auto :** `MvpAcceptanceTest.us08_listBookings`

---

### US-09 — Annuler une réservation

**Priorité :** Must | **Persona :** Parent

> En tant que parent connecté, je veux annuler une réservation.

**Critères d'acceptation :**

- Given réservation confirmée, When DELETE /api/bookings/{id}, Then statut CANCELLED et place libérée
- Given réservation d'un autre parent, When DELETE, Then 403

**Test auto :** `MvpAcceptanceTest.us09_cancelBooking`

---

## EPIC-4 — Administration

### US-10 — Accéder à l'espace administrateur

**Priorité :** Must | **Persona :** Administrateur

> En tant qu'administrateur, je veux accéder à l'espace admin.

**Critères d'acceptation :**

- Given token ADMIN, When GET /api/admin/dashboard, Then 200
- Given token PARENT, When GET /api/admin/dashboard, Then 403

**Test auto :** `MvpAcceptanceTest.us10_adminAccess`

---

### US-11 — Consulter le tableau de bord

**Priorité :** Must | **Persona :** Administrateur

> En tant qu'administrateur, je veux voir un tableau de bord synthétique.

**Critères d'acceptation :**

- Given admin connecté, When GET /api/admin/dashboard, Then totaux users/activities/bookings et recentBookings

**Test auto :** `MvpAcceptanceTest.us11_adminDashboard`

---

### US-12 — Créer une activité

**Priorité :** Must | **Persona :** Administrateur

> En tant qu'administrateur, je veux publier une nouvelle activité.

**Critères d'acceptation :**

- Given admin connecté, When POST /api/activities, Then activité créée avec availableSpots = maxCapacity

**Test auto :** `MvpAcceptanceTest.us12_createActivity`

---

### US-13 — Modifier une activité

**Priorité :** Must | **Persona :** Administrateur

> En tant qu'administrateur, je veux modifier une activité existante.

**Critères d'acceptation :**

- Given admin connecté, When PUT /api/activities/{id}, Then modifications persistées
- Given capacité < places réservées, When PUT, Then 400

**Test auto :** `MvpAcceptanceTest.us13_updateActivity`

---

## EPIC-5 — Notifications

### US-17 — Recevoir un e-mail de bienvenue

**Priorité :** Must | **Persona :** Parent

> En tant que nouveau parent, je veux un e-mail de bienvenue.

**Critères d'acceptation :**

- Given inscription réussie, When événement UserRegistered, Then e-mail visible dans Mailpit

**Test auto :** `NotificationsE2ETest.us17_welcomeEmailAfterRegistration` (avec Mailpit) ou manuel

---

### US-18 — Recevoir un e-mail de confirmation de réservation

**Priorité :** Must | **Persona :** Parent

> En tant que parent, je veux un e-mail après réservation.

**Critères d'acceptation :**

- Given réservation confirmée, When événement BookingConfirmed, Then e-mail parent + admin dans Mailpit

**Test auto :** `EmailServiceTest` + vérification manuelle Mailpit

---

### US-19 — Recevoir un e-mail d'annulation

**Priorité :** Must | **Persona :** Parent

> En tant que parent, je veux un e-mail lors de l'annulation.

**Critères d'acceptation :**

- Given annulation réussie, When événement BookingCancelled, Then e-mail dans Mailpit

**Test auto :** `EmailServiceTest` + vérification manuelle Mailpit

---

## EPIC-6 — Sécurité et architecture

### US-20 — Sécuriser l'API avec JWT

**Priorité :** Must | **Persona :** Équipe technique

> En tant qu'équipe technique, je veux une API sécurisée par JWT.

**Critères d'acceptation :**

- Given pas de token, When route protégée, Then 401 JSON
- Given token PARENT, When route admin, Then 403
- Given token invalide, When route protégée, Then 401

**Test auto :** `MvpAcceptanceTest.us20_jwtSecurity`, `AuthSecurityIntegrationTest`

---

### US-21 — Architecture microservices

**Priorité :** Must | **Persona :** Équipe technique

> En tant qu'équipe technique, je veux une architecture microservices.

**Critères d'acceptation :**

- Given docker compose up, When appel /api via gateway:8080, Then auth/activity/booking/notification répondent
- Given parcours complet parent, When register + book, Then succès bout en bout

**Test auto :** `MvpAcceptanceTest.us21_endToEndFlow`

---

## Récapitulatif

| Epic | Stories | Count |
|------|---------|-------|
| EPIC-1 Découverte | US-01, US-02 | 2 |
| EPIC-2 Auth | US-03, US-04, US-05 | 3 |
| EPIC-3 Réservations | US-07, US-08, US-09 | 3 |
| EPIC-4 Admin | US-10, US-11, US-12, US-13 | 4 |
| EPIC-5 Notifications | US-17, US-18, US-19 | 3 |
| EPIC-6 Sécurité | US-20, US-21 | 2 |
| **Total Must** | | **19** |

---


**Source :** `product/test-matrix.json`

## Niveaux de test

| Niveau | Description |
|--------|-------------|
| **Unitaire** | Tests métier isolés (Mockito) — rapides, sans infra |
| **Intégration** | Tests Spring Boot + MockMvc par microservice |
| **E2E** | Tests REST Assured via gateway — stack Docker requise |

## Matrice User Story → Tests

| US | Titre | Tests automatisés |
|----|-------|-------------------|
| US-01 | Consulter le catalogue | `MvpAcceptanceTest.us01_publicActivitiesList`, `ActivityServiceTest` |
| US-02 | Détail activité | `MvpAcceptanceTest.us02_activityDetail`, `ActivityServiceTest.getActivityById_shouldThrowWhenNotFound` |
| US-03 | Inscription | `MvpAcceptanceTest.us03_registerAndDuplicateEmail`, `AuthServiceTest` |
| US-04 | Connexion | `MvpAcceptanceTest.us04_loginValidAndInvalid`, `AuthServiceTest`, `AuthSecurityIntegrationTest` |
| US-05 | Déconnexion | **Manuel** — Navbar logout UI |
| US-07 | Réserver | `MvpAcceptanceTest.us07_*`, `BookingValidationE2ETest.us07_*`, `BookingServiceTest`, `ActivityServiceTest.reserveSpot_*` |
| US-08 | Mes réservations | `MvpAcceptanceTest.us08_listBookings`, `BookingValidationE2ETest.us08_*` |
| US-09 | Annuler | `MvpAcceptanceTest.us09_cancelBooking`, `BookingValidationE2ETest.us09_*`, `BookingServiceTest.cancelBooking_*` |
| US-10 | Accès admin | `MvpAcceptanceTest.us10_adminAccess` |
| US-11 | Dashboard | `MvpAcceptanceTest.us11_adminDashboard` |
| US-12 | Créer activité | `MvpAcceptanceTest.us12_createActivity`, `ActivityServiceTest.createActivity_*` |
| US-13 | Modifier activité | `MvpAcceptanceTest.us13_updateActivity`, `BookingValidationE2ETest.us13_*`, `ActivityServiceTest.updateActivity_*` |
| US-17 | E-mail bienvenue | `NotificationsE2ETest.us17_*`, `EmailServiceTest.sendWelcomeEmail_*` |
| US-18 | E-mail confirmation | `EmailServiceTest.sendBookingConfirmation_*`, Mailpit manuel |
| US-19 | E-mail annulation | `EmailServiceTest.sendBookingCancellation_*`, Mailpit manuel |
| US-20 | JWT | `MvpAcceptanceTest.us20_jwtSecurity`, `AuthSecurityIntegrationTest` |
| US-21 | Microservices | `MvpAcceptanceTest.us21_endToEndFlow` |

## Fichiers de test

### E2E (`e2e-tests/`)

| Fichier | Rôle |
|---------|------|
| `MvpAcceptanceTest.java` | Parcours MVP principal (US-01 à US-21) |
| `BookingValidationE2ETest.java` | Cas limites réservations et capacité |
| `NotificationsE2ETest.java` | E-mails via API Mailpit (optionnel) |

### Unitaires / intégration (par service)

| Service | Fichiers |
|---------|----------|
| auth-service | `AuthServiceTest.java`, `AuthSecurityIntegrationTest.java` |
| activity-service | `ActivityServiceTest.java` |
| booking-service | `BookingServiceTest.java` |
| notification-service | `EmailServiceTest.java` |

## Commandes

### Tous les tests unitaires

```bash
mvn test -pl services/auth-service,services/activity-service,services/booking-service,services/notification-service
```

### Tests E2E (gateway sur :8080)

```bash
mvn -pl e2e-tests -Pe2e test -De2e.base.url=http://localhost:8080
```

### Tests E2E + Mailpit

```bash
mvn -pl e2e-tests -Pe2e test \
  -De2e.base.url=http://localhost:8080 \
  -De2e.mailpit.enabled=true \
  -De2e.mailpit.url=http://localhost:8025
```

### Collection Postman (tests manuels API)

Importer : `product/postman/Kids-Activities-MVP.postman_collection.json`

Variable d'environnement recommandée : `baseUrl` = `http://localhost:8080`

## Tests manuels restants

| US | Action |
|----|--------|
| US-05 | Se connecter, cliquer Déconnexion, vérifier que localStorage ne contient plus de token |
| US-18 | Réserver une activité, ouvrir Mailpit (:8025), vérifier e-mail confirmation |
| US-19 | Annuler une réservation, vérifier e-mail annulation dans Mailpit |

---


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

---


**Base URL (gateway) :** `http://localhost:8080`

## Authentification

### Inscription

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "nouveau@example.com",
  "password": "motdepasse123",
  "firstName": "Marie",
  "lastName": "Dupont"
}
```

### Connexion

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "parent@example.com",
  "password": "parent123"
}
```

Réponse : `{ "token": "...", "email": "...", "role": "PARENT" }`

### Profil courant

```http
GET /api/users/me
Authorization: Bearer <token>
```

## Activités

| Méthode | Route | Auth | Rôle |
|---------|-------|------|------|
| GET | `/api/activities` | Non | Public (actives) |
| GET | `/api/activities/{id}` | Non | Public |
| POST | `/api/activities` | Oui | ADMIN |
| PUT | `/api/activities/{id}` | Oui | ADMIN |
| DELETE | `/api/activities/{id}` | Oui | ADMIN |

## Réservations

| Méthode | Route | Auth | Rôle |
|---------|-------|------|------|
| GET | `/api/bookings` | Oui | PARENT (ses réservations) |
| POST | `/api/bookings` | Oui | PARENT |
| DELETE | `/api/bookings/{id}` | Oui | PARENT (propre réservation) |

Exemple création :

```http
POST /api/bookings
Authorization: Bearer <token>
Content-Type: application/json

{
  "activityId": 1,
  "childName": "Lucas",
  "childAge": 8
}
```

## Administration (BFF Gateway)

| Méthode | Route | Auth | Rôle |
|---------|-------|------|------|
| GET | `/api/admin/dashboard` | Oui | ADMIN |

Retourne agrégation : totaux utilisateurs, activités, réservations, dernières réservations.

## Collection Postman

**Fichier :** `product/postman/Kids-Activities-MVP.postman_collection.json`

### Import

1. Ouvrir Postman
2. **Import** → sélectionner le fichier JSON
3. Créer un environnement avec :
   - `baseUrl` = `http://localhost:8080`
   - `token` = (rempli après login via script ou manuellement)

### Contenu

La collection couvre les parcours principaux :

- Auth (register, login)
- Activités (liste, détail, CRUD admin)
- Réservations (créer, lister, annuler)
- Admin dashboard
- Cas d'erreur (401, 403, 400)

## Codes HTTP courants

| Code | Situation |
|------|-----------|
| 200 / 201 | Succès |
| 400 | Validation métier (doublon, plus de places, capacité insuffisante) |
| 401 | Token absent ou invalide |
| 403 | Rôle insuffisant (ex. PARENT sur route admin) |
| 404 | Ressource introuvable |

## Swagger (par service, accès direct)

En dev local sans Docker, chaque service expose OpenAPI :

- auth-service : http://localhost:8081/swagger-ui.html
- activity-service : http://localhost:8082/swagger-ui.html
- booking-service : http://localhost:8083/swagger-ui.html

Via gateway, utiliser les routes `/api/**` documentées ci-dessus.

---

