# Règles de développement

Ces règles guident les développements et refactorings d’OnJoue. Pour les comportements produit, consulter [le fonctionnel](docs/fonctionnel.md) ; pour l’interface, [le design system](docs/design-system.md).

## Organisation et responsabilités

- Privilégier `shared/src/commonMain` pour les modèles, la persistance, la présentation et l’UI Compose communes à Android et iOS. Réserver les source sets natifs aux intégrations propres à chaque plateforme.
- Conserver l’injection manuelle des dépendances aux points d’entrée Android/iOS. Ne pas ajouter de framework de DI, de couche `domain` ou de cas d’utilisation sans besoin concret.
- Organiser les repositories et les sources Room par objet métier : `activity`, `tag`, `equipment`, `review`, `favorite`. Garder les migrations et outils de stockage transversaux dans `data.sources.room`.
- Garder les modèles applicatifs dans `data.models`, indépendants de Room. Séparer entités, DAO, relations stockées et mappers ; ne pas exposer les entités Room à l’UI.
- Faire dépendre les ViewModels des contrats de repositories, pas des DAO. Exposer un état UI immuable via `StateFlow` ; faire remonter les événements utilisateur vers le ViewModel. Raccorder les opérations transversales comme l’import par les factories existantes.
- Garder les noms Kotlin en anglais et les textes utilisateur en français. Réutiliser les helpers et conventions existants avant d’ajouter une abstraction.

## Données et persistance

- Conserver Room KMP et SQLite embarqué comme stockage local de référence. Ne pas introduire de stockage factice, de serveur ou de synchronisation implicite.
- Coordonner les écritures d’un agrégat dans une transaction. Pour une activité, remplacer atomiquement les étapes, variantes et associations via leurs DAO respectifs.
- Préserver les identifiants lors des modifications. Les repositories `save` reçoivent le modèle complet, sans générer son identifiant ; le ViewModel génère celui d’un nouvel élément de gestion.
- Ne pas remplacer un upsert d’entité métier par une suppression suivie d’une insertion : cela détruirait les relations, avis et favoris. Conserver la logique de mise à jour puis insertion des DAO.
- Enregistrer les tags et matériels séparément des activités. Sauvegarder une activité ne doit pas créer ou modifier implicitement ces catalogues.
- Conserver les contraintes SQLite : unicité des libellés normalisés, unicité d’un favori par activité et clés étrangères. Refuser la suppression d’un tag parent ou d’un élément utilisé ; préserver les cascades des dépendances lors de la suppression d’une activité.
- Nettoyer les libellés avec `trim()` et normaliser avec le `lowercase()` Unicode de Kotlin, pas le `NOCASE` ASCII de SQLite. L’unicité des tags s’applique par groupe, celle des matériels à tout leur catalogue.
- Garder les sept groupes fixes. N’autoriser un parent que pour un lieu, lui-même rattaché à un lieu racine : deux niveaux maximum.
- Pour toute évolution du schéma, ajouter une migration non destructive, incrémenter la version et exporter le schéma dans `shared/schemas`. Préserver les schémas historiques et le fichier de base `onjoue.db`.

## Catalogue initial

- Modifier `shared/src/commonMain/composeResources/files/initial-catalog.json` plutôt que réintroduire des données de seed en Kotlin. Garder les identifiants stables et les références cohérentes.
- Charger et valider le JSON avec l’import suspendu existant avant de rendre l’application disponible. Ne pas bloquer le thread principal pour charger une ressource depuis un callback Room synchrone.
- Respecter le marqueur `catalog_state` : une version déjà importée ne doit pas réinitialiser les modifications ou suppressions à chaque lancement. Faire évoluer explicitement la version du catalogue lorsqu’un nouvel import automatique est souhaité.
- Conserver l’import et son marqueur dans la même transaction. Réutiliser les identifiants des tags/matériels existants de même libellé normalisé ; signaler les collisions incompatibles plutôt que les écraser silencieusement.
- Ne pas exposer de remplacement forcé dans l’application utilisateur. Conserver les opérations de persistance nécessaires aux imports et futures synchronisations, sans effacer l’historique des activités conservées.

## Présentation et navigation

- Utiliser les routes typées et sérialisables de Navigation 3. Enregistrer toute nouvelle route dans la configuration de sérialisation et conserver les piles et états indépendants des onglets.
- Préserver les règles de Retour et les états des onglets. Retirer les anciennes routes de gestion restaurées.
- Ne pas exposer les mutations éditoriales dans l’application utilisateur. Préserver les gardes des repositories tant que leurs opérations de gestion existent ; conserver séparément les écritures internes nécessaires aux imports et futures synchronisations. Ne pas restreindre les opérations d’avis/favoris prévues en release.
- Présenter explicitement chargement, erreur, état vide et fonctionnalité indisponible. Ne pas remplir un écran de production avec des données fictives ou un succès de substitution.
- Construire l’UI uniquement avec l’API publique de `:designsystem` : `OnJoueTheme`, composants `Ds*`, `Spacing` (pas utilisés tels quels avec `Modifier.padding`, `Arrangement.spacedBy`, `Spacer`, sans calcul), `TextRole`/`TextTone` et autres enums sémantiques. Hors de ce module, ne jamais utiliser `Color(...)`, de littéraux `dp`/`sp`, `MaterialTheme`, `Text` ni d’import `androidx.compose.material*`.
- Si un besoin n’est pas couvert, ajouter ou étendre un composant dans `:designsystem` (tokens `internal`, valeurs sémantiques publiques) plutôt que contourner les règles par `@Suppress`. Suivre le design system sans figer les écrans sur une seule taille ou le seul thème clair.

## Erreurs et concurrence

- Réutiliser `AppException` et la traduction SQLite centralisée. Conserver la cause ; ne pas assimiler une erreur de programmation à une erreur de stockage.
- Utiliser `viewModelAction` pour les actions suspendues ponctuelles, avec traitement d’erreur et nettoyage de l’état occupé. Traiter les erreurs de flux en amont du collecteur.
- Passer les erreurs utilisateur par `ErrorParser`/`Message`, avec une possibilité de réessayer lorsque pertinente. Ne pas afficher les diagnostics techniques ni journaliser de données privées.
- Propager `CancellationException`. Éviter les captures générales qui masquent une erreur, les valeurs par défaut de substitution et les mutations concurrentes répétées pendant une opération occupée.

## Validation des changements

Exécuter la commande la plus ciblée couvrant le changement, puis élargir si nécessaire :

| Changement | Validation |
|---|---|
| Tout changement Kotlin ou de configuration Detekt | `./gradlew detekt` |
| Règles Detekt personnalisées | `./gradlew :detekt-rules:test` |
| Android ou présentation partagée | `./gradlew :androidApp:compileDebugKotlin` |
| Tests partagés et persistance iOS | `./gradlew :shared:iosSimulatorArm64Test` |
| Persistance ou UI host Android | `./gradlew :shared:testAndroidHostTest` avec `--tests` si pertinent |
| Compilation des tests host Android | `./gradlew :shared:compileAndroidHostTest` |
| Rendus Compose | `./gradlew :androidApp:validateDebugScreenshotTest` |

- **Avant de terminer toute tâche modifiant du Kotlin, exécuter `./gradlew detekt` et corriger toutes les violations** ; pas de baseline ni de `@Suppress` pour les règles du design system. Le même contrôle bloque le hook pre-commit et la CI.
- Adapter les tests aux changements de comportement, notamment migrations, atomicité, contraintes, import idempotent et remplacement forcé.
- Faire approuver visuellement les nouvelles références de captures avant de les mettre à jour ; ne pas régénérer les références pour masquer une régression.
- Signaler les validations bloquées ou échouées ; ne pas les présenter comme réussies. Une modification exclusivement documentaire ne nécessite pas de compilation.
- Mettre à jour la documentation directement concernée et préserver les changements préexistants du worktree.
