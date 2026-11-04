# OnJoue?

Une app mobile pour trouver en 30 secondes une activité adaptée à l'âge de l'enfant, à l'environnement et au besoin du moment (se dépenser, se calmer).

## Fonctionnement

L'utilisateur répond à trois questions :

1. **Combien de temps ?** 5, 15 ou 30 min
2. **Où ?** Dedans, dehors ou en déplacement
3. **Quel besoin ?** On se dépense ou on se calme
   L'âge de l'enfant et le matériel disponible à la maison sont appliqués automatiquement. Des raccourcis couvrent les situations courantes (« Il pleut », « On part en voyage »).

## Modèle de données

Peu de champs, beaucoup de tags.

- **Champs :** nom, description, étapes (minuteur optionnel), note, variantes, âge (min/max en mois), durée (min), matériel, image, document imprimable.
- **Tags :** un libellé et un groupe (Type, Lieu, Occasion, Saison, Moment, Participants, Ambiance). Le lieu a deux niveaux (Dedans > Salon).
- **Filtrage :** OU dans un groupe, ET entre groupes. Un groupe vide sur la fiche signifie aucune contrainte. Un tag parent inclut ses enfants.


## Règles éditoriales

- Vocabulaire de tags fermé, pas de tags libres
- Un tag de type et au moins un tag d'ambiance par fiche
- 8 tags maximum par fiche, environ 40 tags au total
- Un nouveau tag n'est ajouté que s'il filtre au moins 5 fiches

La règle des cinq fiches est une consigne éditoriale : elle ne bloque pas la création de tags dans l'outil debug.

## Gestion des tags et des matériels (debug)

Sur Android et iOS, **Paramètres** propose en debug **Gérer les tags**, **Gérer les matériels** et **Gérer les activités**. Les deux premières entrées ouvrent directement la catégorie correspondante pour rechercher, créer, modifier et supprimer les éléments du catalogue. Les tags peuvent aussi être filtrés par groupe. Ces écrans et leurs opérations de modification sont réservés aux builds **debug** ; aucun accès à la gestion n'est proposé en release. La gestion des activités n'est pas encore disponible.

### Navigation

Les quatre onglets **Idée**, **Fiches**, **Favoris** et **Paramètres** utilisent Navigation 3 et des routes Kotlin typées et sérialisables, dans le code partagé Android/iOS. La navigation Material 3 adapte la barre basse à un rail latéral sur les fenêtres larges. Idée est l'onglet initial ; chaque onglet conserve son historique et ses états lors des changements d'onglet et de la restauration.

Depuis une gestion, Retour revient à Paramètres. Depuis la racine d'un autre onglet, Retour revient à Idée ; à la racine Idée, Android gère la sortie. Retoucher l'onglet actif ne réinitialise pas les brouillons. Les routes de gestion restaurées sans capacité debug sont retirées avant affichage.

Les écrans Idée, Fiches, Favoris et gestion des activités affichent explicitement leur indisponibilité, sans données fictives. Seule la gestion des tags/matériels est fonctionnelle à ce stade. Aucun réglage supplémentaire ni stockage de préférences n'est inventé.

Le thème partagé reprend une palette vert pétrole, un fond gris clair, des cartes blanches arrondies et des actions de suppression orange, avec une variante sombre. Les tags sont regroupés par groupe et les enfants suivent leur parent. Les badges affichent le nombre réel d'activités, calculé et observé dans Room : pour un tag parent, ses associations et celles de ses enfants sont réunies sans compter deux fois une activité. Les matériels affichent leurs associations directes. Ces compteurs sont informatifs et ne modifient pas les règles de suppression.

Les écrans de gestion affichent le titre et une flèche de retour standard dans la top bar. La création utilise un FAB ; les groupes sont sélectionnés dans un dropdown, dans les filtres comme dans le formulaire. Les actions crayon/poubelle sont à droite des cartes, avec des descriptions accessibles et des cibles de 48 dp. Les petites fenêtres et grandes polices conservent un repli vertical. Les catégories restent accessibles séparément depuis Paramètres, sans barre interne d'onglets de gestion.

Dans la liste des tags, chaque en-tête de groupe est repliable avec un chevron. Les groupes sont ouverts par défaut ; leur état est conservé à la restauration de l'écran. Le compteur indique le nombre de tags correspondant au filtre, même lorsque le groupe est fermé.

Les captures Compose sont configurées dans `androidApp/src/screenshotTest` pour neuf tailles, le mode sombre et une grande police. Avec AGP 9.1, elles utilisent le plugin standalone déprécié ; les nouvelles test suites nécessitent AGP 9.5 ou supérieur, sans mise à jour AGP dans cette étape. `:androidApp:validateDebugScreenshotTest` génère les rendus et les compare aux références ; les premières références doivent être approuvées visuellement avant `:androidApp:updateDebugScreenshotTest`. Le rapport est dans `androidApp/build/reports/screenshotTest/preview/debug/index.html`.

Les données sont conservées localement avec **Room Kotlin Multiplatform** et le pilote SQLite embarqué, dans la base privée `onjoue.db`, sans serveur ni synchronisation entre appareils.  Les mutations sont transactionnelles ; une erreur est affichée avec possibilité de réessayer, sans réinitialisation destructive de la base.

Les libellés sont obligatoires et nettoyés de leurs espaces de début et de fin. Les doublons sont refusés sans distinction de casse, par groupe pour les tags et dans tout le catalogue pour les matériels. Les sept groupes sont fixes. Seuls les lieux acceptent un parent, lui-même un lieu sans parent : la hiérarchie reste limitée à deux niveaux.

La suppression exige une confirmation et est refusée pour un tag ayant des enfants ou pour un élément associé à une activité. Un renommage conserve l'identifiant et les associations. Ces protections reposent sur les clés étrangères SQLite de la même base, pas sur des références simulées.

Les activités complètes (étapes et variantes ordonnées, tags, matériels), les avis et les favoris sont également persistés dans Room. Leurs repositories sont disponibles sans nouveaux écrans dédiés. Les mutations d'activités sont réservées au debug ; les avis et favoris restent modifiables en release. Une activité ne possède qu'un favori. Supprimer une activité supprime ses étapes, variantes, associations, avis et favoris, mais conserve les tags et les matériels.

`Activity` embarque ses étapes (`List<Step>`), variantes (`List<String>`), tags (`Set<Tag>`) et matériels (`Set<Equipment>`), pas seulement les identifiants du catalogue. Room charge les objets complets via les tables de jointure ; un renommage du catalogue est donc reflété dans les activités lues et observées. Enregistrer une activité met à jour ses associations, sans créer ni modifier implicitement les tags et matériels : ceux-ci sont enregistrés via leurs propres repositories.

Les repositories d'activités, tags, matériels et avis exposent une méthode `save` unique recevant leur modèle complet (`Activity`, `Tag`, `Equipment`, `Review`), sans valeur de retour : elle insère si l'identifiant est absent en base et met à jour s'il existe. Pour les tags et matériels, le ViewModel génère l'identifiant à la création et conserve celui du modèle lors des modifications ; les repositories ne génèrent aucun identifiant. L'upsert Room ne supprime pas la ligne existante : les associations, avis et favoris sont conservés. Les mises à jour d'activités remplacent atomiquement leurs étapes, variantes et associations.

Les identifiants textuels sont stables. Les index uniques SQLite continuent de refuser les conflits de libellés et les doublons de favoris, sans régénération silencieuse. Les libellés normalisés utilisent le `lowercase()` Unicode de Kotlin, y compris pendant la migration, plutôt que le `NOCASE` ASCII de SQLite.

### Organisation

- `data.models` : modèles applicatifs indépendants de Room.
- `data.repositories.activity`, `tag`, `equipment`, `review`, `favorite` : contrats et implémentations Room regroupés par objet métier.
- `data.repositories.common` : règles partagées, validation des libellés, garde debug et contrôle des lignes affectées.
- `data.sources.room` : base Room, constructeurs Android/iOS, traduction des erreurs et transactions.
- `data.sources.room.activity`, `tag`, `equipment`, `review`, `favorite` : fichiers regroupés par objet métier, chaque package réunissant ses DAO, entités et conversions. Le package `activity` contient aussi les étapes, variantes, associations et relations de l'agrégat.
- `data.sources.room.migrations` : migrations transversales de la base.
- `presentation.management` : écrans Compose, état UI et ViewModel, dépendant des contrats de repositories.
- `presentation.navigation` : routes sérialisables, piles par onglet, navigateur et conteneur Material 3 adaptatif.
- `presentation.settings` : entrées de gestion debug, sans accès direct aux repositories.
- `presentation.common` : état explicite des fonctionnalités non disponibles.
- `presentation.theme` : palette claire/sombre, typographie et formes communes.
- `presentation.errors` : `ErrorParser`, `Message` et `viewModelAction`, communs à la présentation.

Les noms Kotlin sont en anglais ; l'interface reste en français. Il n'y a ni couche `domain`, ni cas d'utilisation, ni stockage factice en production, ni fichier JSON parallèle. Aucune préférence n'est nécessaire pour ces données.

`ActivityDao` se limite à la lecture de l'agrégat et à l'écriture des champs de l'activité. `StepDao`, `VariantDao`, `ActivityTagDao` et `ActivityEquipmentDao` gèrent chacun leurs tables ; `RoomActivityRepository` les coordonne dans une même transaction. `ActivityEntity`, `StoredActivity` (relations Room) et `ActivityMapper` sont séparés du DAO. Ce découpage ne modifie pas le schéma SQLite.

Les avis et favoris disposent chacun de leur `ReviewDao` / `FavoriteDao`, de leur entité et de leur mapper ; aucun fichier `UserDataDao` ne mélange ces responsabilités. Les schémas de la base déplacée sont exportés sous `shared/schemas/com.sotopatrick.onjoue.data.sources.room.AppDatabase`, avec les versions historiques conservées. Le nom du fichier de base reste inchangé.

### Erreurs et migrations

Les repositories lèvent des `AppException` typées : validation, conflit, absence, utilisation, interdiction, référence invalide, données corrompues ou stockage. La traduction des erreurs SQLite est centralisée dans `data.sources` et conserve la cause. Les erreurs de programmation ne sont pas assimilées à des erreurs de stockage. L'API KMP `SQLiteException` n'exposant pas de propriété de code numérique, le traducteur reconnaît les codes étendus lorsqu'ils figurent dans le diagnostic et les diagnostics de contraintes SQLite.

Le ViewModel confie les actions suspendues à `viewModelAction`, avec un traitement d'erreur obligatoire et un nettoyage final. Les erreurs des flux sont traitées uniquement en amont du collecteur. `ErrorParser` traduit les exceptions en `Message` français, sans afficher les diagnostics techniques. Les annulations sont propagées, sans erreur utilisateur ; les erreurs inattendues sont signalées localement par leur type, sans données privées.

Les schémas v1, v2 et v3 sont exportés dans `shared/schemas`. La migration `Migration1To2` conserve les données historiques et ajoute les contraintes. `Migration2To3` installe une seule fois le catalogue initial ; un callback Room fait la même installation à la création d'une base. Le fichier reste `onjoue.db` : aucune migration destructive n'est activée.

Le catalogue livré en debug **et en release** contient 41 tags couvrant les sept groupes (dont dix lieux enfants) et 24 matériels utiles aux activités : papier, crayons, ballon, pâte à modeler, etc. L'installation conserve les libellés, identifiants et relations existants ; les doublons de libellés normalisés sont réutilisés. Les collisions d'identifiants restent des erreurs du stockage, sans remplacement silencieux. Une hiérarchie existante incompatible empêche l'installation avec une erreur typée. La création et les migrations Room sont transactionnelles. Les modifications et suppressions ultérieures ne sont pas réinitialisées à la réouverture.

Android et iOS ouvrent la base au démarrage dans les deux configurations. Un chargement est affiché pendant cette ouverture ; les erreurs passent par `FrenchErrorParser` avec une action « Réessayer ». L'installation du catalogue ne donne aucun droit d'édition en release : les mutations restent réservées au debug.
