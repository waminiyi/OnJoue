# Design system UI

L’interface partagée Android/iOS utilise **Compose Multiplatform et Material 3**, encapsulés dans le module **`:designsystem`**. Elle privilégie une lecture rapide, des surfaces aérées et des actions explicites. Le thème suit le mode clair/sombre du système ; aucun sélecteur de thème n’est proposé.

## Module `:designsystem`

Le module expose des **composants** et des **valeurs sémantiques**, jamais de tokens bruts (`Color`, `TextUnit`, `TextStyle`, `Shape`). Seule l’échelle `Spacing` est publique en `Dp`, pour s’utiliser directement avec les API Compose de mise en page. Les écrans de `shared` et `androidApp` n’utilisent que cette API publique.

| Paquet | API publique |
|---|---|
| `designsystem` | `OnJoueTheme(darkTheme) { … }` |
| `designsystem.foundation` | `Spacing` (`None` … `XXLarge`, constantes `Dp` à utiliser telles quelles avec `Modifier.padding`, `Arrangement.spacedBy`, `Spacer`), `Breakpoint` + `DsWidthBreakpoint` |
| `designsystem.components` | Texte : `DsText` avec `TextRole` et `TextTone`. Actions : `DsButton`, `DsTextButton`, `DsOutlinedIconButton`, `DsFloatingActionButton`, `DsFilterChip`, `ActionTone`. Surfaces : `DsCard`, `DsActionCard`, `DsCallout`, `DsHeroBanner`, `DsSection`, `DsScreenHeader`, `DsBackground`, `DsLoadingState`, `DsErrorState`. Contenu : `DsBadge`, `DsIllustration`, `DsMonogram`, `DsNumberedItem`, `DsBulletItem`. Champs : `DsSearchField`, `DsTextField`, `DsSelectField`. Dialogues : `DsConfirmDialog`, `DsFormDialog`. Structure : `DsScaffold`, `DsAdaptiveGrid`, `DsCollapsibleHeader`, `DsNavigationScaffold` |

Les tokens (`tokens/ColorTokens.kt`, `TypographyTokens.kt`, `ShapeTokens.kt`, `DimensionTokens.kt`) sont `internal`. Le module est compilé en `explicitApi()` : toute déclaration publique est un choix délibéré.

### Règles appliquées par Detekt

Hors de `:designsystem` (et hors tests), `./gradlew detekt` échoue sur :

| Règle (`onjoue-design-system`) | Interdit |
|---|---|
| `RawColorUsage` | `Color(...)`, `Color.Red`, etc. |
| `HardcodedDimension` | littéraux `.dp` / `.sp` / `.em`, `Dp(...)`, `DpSize(...)`, `toDp()` / `toSp()` |
| `SpacingArithmetic` | calculs sur `Spacing` (`Spacing.Medium + 4.dp`, `Spacing.Large * 2`, `-Spacing.Small`) |
| `DirectMaterialThemeUsage` | `MaterialTheme.*` |
| `DirectTextUsage` | `Text(...)` (utiliser `DsText`) |
| `ForbiddenMaterialImport` | imports `androidx.compose.material*` (le module seul en dépend) |

Les règles [compose-rules](https://mrmans0n.github.io/compose-rules/) et le socle Detekt complètent ces contrôles. La configuration est dans `config/detekt/detekt.yml`, les règles personnalisées dans le module `:detekt-rules`. Il n’y a pas de baseline : toute violation doit être corrigée, pas ignorée.

Detekt s’exécute en CI (`.github/workflows/ci.yml`) et dans le hook `.githooks/pre-commit`, activé automatiquement par la tâche Gradle `installGitHooks` (`git config core.hooksPath .githooks`).

### Ajouter ou faire évoluer un composant

1. Chercher un composant `Ds*` ou une valeur sémantique existante avant d’en créer un.
2. Ajouter le composant dans `designsystem/components`, en le nommant `Ds…` et en exposant des enums sémantiques (`…Tone`, `…Style`, `…Size`) plutôt que des couleurs, tailles ou styles.
3. Garder les nouvelles valeurs dans `tokens/` en `internal` ; n’ajouter une entrée à `Spacing` ou `Breakpoint` que si elle sert plusieurs écrans, plutôt que de dériver une valeur d’un pas existant.
4. Vérifier avec `./gradlew detekt :androidApp:compileDebugKotlin` et, pour un rendu modifié, `./gradlew :androidApp:validateDebugScreenshotTest`.

## Couleurs sémantiques

Valeurs internes du module, exposées aux écrans uniquement via `TextTone`, `ActionTone`, `BadgeTone`, etc.

| Rôle | Clair | Sombre | Usage |
|---|---|---|---|
| `primary` | `#0B7B72` | `#80D5C9` | Actions et sélection, vert pétrole |
| `primaryContainer` | `#DDF3EF` | `#174B45` | Badges, illustrations et bandeau de détail |
| `background` | `#F4F6F8` | `#0F1A20` | Fond d’écran |
| `surface` | `#FFFFFF` | `#18282F` | Cartes et surfaces de contenu |
| `onSurface` | `#13232D` | `#E3EEF3` | Texte principal |
| `onSurfaceVariant` | `#64798B` | `#A9BDCD` | Texte secondaire |
| `outlineVariant` | `#DFE7ED` | `#344852` | Bordures discrètes |
| `error` | `#BD3700` | `#FFB69A` | Erreurs et actions destructives |

Associer chaque fond à son rôle de contenu (`onPrimary`, `onPrimaryContainer`, etc.). Les autres variantes de surface et de couleur restent définies dans [ColorTokens.kt](../designsystem/src/commonMain/kotlin/com/sotopatrick/onjoue/designsystem/tokens/ColorTokens.kt).

## Typographie, formes et rythme

- Typographie Material 3 par défaut, sans police personnalisée, choisie via `TextRole` : titres `Headline*` en gras, `TitleLarge`/`TitleMedium` et `LabelLarge`/`LabelMedium` en semi-gras. Utiliser `BodyLarge` pour la lecture, `BodyMedium` pour les aperçus et les rôles `Label*` pour les badges.
- Rayons du thème : **8 / 12 / 16 / 24 / 28 dp**, de `extraSmall` à `extraLarge`. Privilégier ces formes pour les nouveaux composants.
- Espacements via l’échelle `Spacing` (4 à 24 dp, usuels : `Small` 8, `Medium` 12, `Large` 16, `ExtraLarge` 20, `XXLarge` 24). Les marges des listes sont généralement `ExtraLarge`, celles des paramètres `XXLarge`.
- Les formes propres à un composant (recherche 18 dp, illustration 20 dp, bandeau 24 dp) restent internes au composant qui les utilise.

## Composants et compositions

| Élément | Convention actuelle |
|---|---|
| Navigation | Icône et libellé français ; barre basse ou rail Material 3 ; sélection en couleur primaire |
| Carte d’activité | Surface, rayon 24 dp, bordure 1 dp, élévation 1 dp ; illustration emoji sur fond teinté ; nom sur une ligne et aperçu sur deux |
| Badges | Forme pilule, `primaryContainer` ; âge, durée, ambiance ; défilement horizontal si nécessaire |
| Recherche et filtres | Champ bordé ; chips pour l’âge des fiches ; dropdown pour les groupes de tags |
| Détail | Bandeau teinté, titre et résumé, étapes numérotées, matériel, note mise en évidence, variantes et source |
| Gestion | Top bar avec Retour, FAB de création, cartes avec actions modifier/supprimer et compteur d’usage |
| Paramètres | Cartes d’actions avec titre et description ; outils debug regroupés |
| Confirmation | Dialogue avec conséquence explicite et actions confirmer/annuler ; suppression et remplacement du catalogue |

Les illustrations d’activités sont actuellement des emojis sélectionnés par le code, pas une bibliothèque d’images ni des photos du catalogue.

## États et interactions

Afficher un indicateur pendant les chargements, un message d’erreur français et une action « Réessayer » lorsque le parcours la prévoit. Distinguer une liste vide, une recherche sans résultat et une fonctionnalité indisponible. Après une mutation, rendre le succès ou l’erreur visible ; bloquer les actions concurrentes de gestion pendant l’écriture.

Conserver recherche, filtres et brouillons lors des changements d’onglet et de la restauration lorsque l’écran les enregistre. Les composants de gestion utilisent des descriptions d’actions pour les icônes et un état sémantique pour les sections repliables.

## Adaptation et accessibilité

- Préserver les zones sûres autour du contenu ; les formulaires de gestion prennent aussi en compte le clavier.
- La gestion utilise une grille adaptative de 560 dp minimum par colonne. Ses champs se réorganisent selon la largeur et l’échelle de police ; ne pas imposer une disposition horizontale aux petites fenêtres.
- Les actions modifier/supprimer de gestion disposent de cibles de 48 dp. Donner un libellé accessible à toute nouvelle icône d’action ; une couleur seule ne doit pas porter le sens d’une action.
- Vérifier les nouveaux écrans en clair/sombre, petite/grande fenêtre et grande police. Les fiches restent aujourd’hui une liste à une colonne ; ne pas leur attribuer un comportement multi-pane inexistant.

Ces conventions ne constituent pas une certification d’accessibilité. Les previews et captures Compose dans `androidApp/src/screenshotTest` servent à examiner plusieurs tailles, le mode sombre et la grande police ; leurs références doivent être approuvées visuellement.

Sources complémentaires : [fiches et détail](../shared/src/commonMain/kotlin/com/sotopatrick/onjoue/presentation/activity/ActivitySheets.kt), [gestion](../shared/src/commonMain/kotlin/com/sotopatrick/onjoue/presentation/management/TagEquipmentManagementScreen.kt), [navigation](../shared/src/commonMain/kotlin/com/sotopatrick/onjoue/presentation/navigation/AppNavigationScaffold.kt).
