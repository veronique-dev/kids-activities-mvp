# API et Postman

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
