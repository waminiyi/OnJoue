# OnJoue?

Une application Android et iOS pour trouver une activité à partager avec un enfant, construite avec Kotlin Multiplatform et Compose Multiplatform.

Le catalogue local permet de rechercher des fiches, de filtrer par tranche d’âge et de consulter leurs étapes et matériels. Les outils de gestion éditoriale ne sont plus présents dans l’application. Le parcours de recommandation et l’écran des favoris ne sont pas encore disponibles.

## Documentation

| Document | Contenu |
|---|---|
| [Règles de développement — AGENTS.md](AGENTS.md) | Consignes techniques pour les agents, développements et refactorings |
| [Fonctionnel](docs/fonctionnel.md) | Parcours disponibles, règles métier, catalogue et limites actuelles |
| [Design system](docs/design-system.md) | Thème, composants, états UI et conventions visuelles |

Le code applicatif partagé se trouve dans `shared`, l’entrée Android dans `androidApp` et l’hôte iOS dans `iosApp`. Les données sont stockées localement avec Room et SQLite embarqué, sans serveur ni synchronisation.

## Qualité

`./gradlew detekt` vérifie le style Kotlin, les règles Compose et le respect du design system (module `:designsystem`). Il s’exécute en CI et dans le hook pre-commit versionné dans `.githooks`, activé automatiquement par toute compilation Gradle (ou manuellement : `./gradlew installGitHooks`).
