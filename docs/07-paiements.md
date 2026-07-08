# Paiements

Le MVP intègre un **payment-service** qui gère le règlement des réservations payantes.

## Flux

1. Le parent crée une réservation → statut `PENDING_PAYMENT` (activité avec prix > 0)
2. Le frontend appelle `POST /api/payments/checkout` avec le `bookingId`
3. Le parent paie via **Stripe Checkout** (si activé) ou en **mode simulé** (MOCK)
4. Le payment-service confirme la réservation → statut `CONFIRMED`, place réservée, e-mail envoyé

Les activités **gratuites** (prix = 0) restent confirmées immédiatement sans étape de paiement.

## API

| Méthode | Route | Description |
|---------|-------|-------------|
| POST | `/api/payments/checkout` | Créer une session de paiement |
| POST | `/api/payments/{id}/complete-mock` | Finaliser un paiement simulé (MOCK) |
| POST | `/api/payments/verify-session?sessionId=` | Confirmer après retour Stripe |
| POST | `/api/payments/webhook/stripe` | Webhook Stripe (serveur) |
| GET | `/api/payments/{id}` | Consulter un paiement |

## Mode simulation MOCK (défaut)

Sans configuration Stripe, le provider `MOCK` est utilisé. Cliquez sur « Payer » dans l'interface.

## Stripe (mode test)

### 1. Créer un compte Stripe

https://dashboard.stripe.com/register → mode **Test** activé par défaut.

### 2. Récupérer les clés

Dans [API Keys (test)](https://dashboard.stripe.com/test/apikeys) :
- **Secret key** : `sk_test_...`

### 3. Configurer le projet

Copiez `.env.example` vers `.env` :

```bash
cp .env.example .env
```

Éditez `.env` :

```env
STRIPE_ENABLED=true
STRIPE_SECRET_KEY=sk_test_votre_cle
APP_FRONTEND_URL=http://localhost:5173
```

### 4. Redémarrer Docker

```bash
docker compose up --build -d payment-service frontend
```

### 5. Tester un paiement

1. Connectez-vous en parent
2. Réservez une activité payante
3. Cliquez **« Payer avec Stripe »** → redirection vers Stripe Checkout
4. Carte test : `4242 4242 4242 4242` — date future — CVC quelconque
5. Retour automatique sur « Mes réservations » → réservation **Confirmée**

### 6. Webhook (optionnel en local)

Pour la confirmation via webhook (recommandé en production) :

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook/stripe
```

Copiez le `whsec_...` affiché dans `.env` → `STRIPE_WEBHOOK_SECRET`.

> En local, la confirmation fonctionne aussi via `verify-session` au retour de Stripe (sans webhook).

## Architecture

```
Frontend → Gateway → payment-service → Stripe Checkout
                              ↓
                     booking-service (confirm)
```

- Base de données : `payment_db`
- Port interne : `8085`

## Statuts

| Booking | Signification |
|---------|---------------|
| `PENDING_PAYMENT` | Réservation créée, paiement en attente |
| `CONFIRMED` | Payé et place réservée |
| `CANCELLED` | Annulée |

| Payment | Signification |
|---------|---------------|
| `PENDING` | En attente |
| `COMPLETED` | Payé |
| `FAILED` | Échec |
| `REFUNDED` | Remboursé |

| Provider | Usage |
|----------|-------|
| `MOCK` | Développement / démo sans Stripe |
| `STRIPE` | Paiement réel (mode test ou live) |
