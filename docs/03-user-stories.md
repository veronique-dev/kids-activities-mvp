# User stories MVP

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
