# Kids Activities MVP

Plateforme de réservation d'activités pour enfants — architecture microservices (Spring Boot + React).

## Documentation

Toute la documentation produit, technique et QA est centralisée dans le dossier **[`docs/`](docs/README.md)** :

| Document | Contenu |
|----------|---------|
| [Démarrage rapide](docs/01-demarrage-rapide.md) | Lancer le projet (Docker / local) |
| [Architecture](docs/02-architecture.md) | Microservices, ports, flux |
| [User stories](docs/03-user-stories.md) | 19 stories MVP avec critères d'acceptation |
| [Plan de test](docs/04-plan-de-test.md) | Matrice tests ↔ user stories |
| [Configuration](docs/05-comptes-et-configuration.md) | Comptes démo, variables d'environnement |
| [API & Postman](docs/06-api-et-postman.md) | Endpoints et collection Postman |

### Fichiers sources (JSON / CSV)

- `product/user-stories-mvp.json` — user stories structurées
- `product/user-stories-mvp.csv` — import Jira / Excel
- `product/test-matrix.json` — couverture de tests
- `product/postman/Kids-Activities-MVP.postman_collection.json` — tests API manuels

### Guide PDF

```bash
./scripts/generate-docs-pdf.sh
```

Le PDF est généré dans `docs/pdf/Kids-Activities-MVP-Guide-complet.pdf`.

## Démarrage express (Docker)

```bash
docker compose up --build
```

- Frontend : http://localhost:5173
- API Gateway : http://localhost:8080
- Mailpit (e-mails) : http://localhost:8025
