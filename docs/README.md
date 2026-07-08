# Documentation Kids Activities MVP

Bienvenue dans la documentation du projet. Tous les documents sont en **Markdown** (lisibles dans Cursor, sur GitHub, ou convertibles en PDF).

## Où consulter la documentation ?

| Emplacement | Comment y accéder |
|-------------|-------------------|
| **Local (Cursor)** | Dossier `docs/` à la racine du projet |
| **GitHub (en ligne)** | Après `git push` : `https://github.com/veronique-dev/kids-activities-mvp/tree/main/docs` |
| **PDF** | Exécuter `./scripts/generate-docs-pdf.sh` → `docs/pdf/Kids-Activities-MVP-Guide-complet.pdf` |

## Index des documents

1. **[Démarrage rapide](01-demarrage-rapide.md)** — installation, Docker, tests
2. **[Architecture](02-architecture.md)** — microservices, gateway, RabbitMQ, bases de données
3. **[User stories MVP](03-user-stories.md)** — epics, stories, Given/When/Then
4. **[Plan de test](04-plan-de-test.md)** — unitaires, intégration, E2E, commandes Maven
5. **[Comptes & configuration](05-comptes-et-configuration.md)** — identifiants démo, ports, secrets
6. **[API & Postman](06-api-et-postman.md)** — routes gateway, collection Postman

## Fichiers produit (sources)

Ces fichiers restent dans `product/` pour l'import outils (Jira, Postman, CI) :

```
product/
├── user-stories-mvp.json      # Source de vérité user stories
├── user-stories-mvp.csv       # Export CSV (Jira / Excel)
├── test-matrix.json           # Matrice couverture tests
└── postman/
    └── Kids-Activities-MVP.postman_collection.json
```

## Guide PDF complet

Le script `scripts/generate-docs-pdf.sh` assemble tous les chapitres en un seul PDF. Prérequis : Node.js (recommandé) ou Pandoc.
