# Plan de test

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
