# Fonctionnement d’OnJoue

OnJoue aide à trouver une activité à partager avec un enfant. L’objectif produit est de proposer rapidement une idée selon l’âge, le temps disponible, le lieu et le besoin du moment. Le parcours de recommandation n’est pas encore disponible ; la consultation des fiches l’est.

## Périmètre actuel

| Fonction | État |
|---|---|
| Fiches : catalogue, recherche, filtre d’âge et détail | Disponible en debug et release |
| Gestion des tags et matériels | Disponible uniquement en debug |
| Remplacement du catalogue initial | Disponible uniquement en debug |
| Onglets Idée et Favoris ; gestion des activités | Écrans explicitement indisponibles |
| Avis et favoris ; création/modification d’activités | Modèles et repositories présents, sans parcours UI dédié |
| Profil enfant, matériel du foyer, préférences et synchronisation | Non implémentés |

Les données sont conservées sur l’appareil, sans compte ni serveur. Le catalogue est livré avec l’application ; seul l’accès à une source externe nécessite d’ouvrir son lien.

## Consulter les fiches

- Le catalogue livré contient **41 activités, 41 tags et 57 matériels**, issus des trois sources Naître et grandir : [31 activités pour bouger](https://naitreetgrandir.com/fr/etape/5-8-ans/jouer-bouger/31-activites-bouger-avec-enfant/), [Acrobaties géométriques](https://naitreetgrandir.com/fr/etape/3-5-ans/fiches-activites/jeux/acrobaties-geometriques/) et [Jeux maison](https://naitreetgrandir.com/fr/etape/3-5-ans/fiches-activites/jeux/jeux-maison/). Les descriptions et étapes sont reformulées.
- Dans **Fiches**, la recherche porte sur le nom, la description, les tags et les matériels, sans distinction de casse.
- Les filtres **Toutes**, **1–3 ans**, **3–5 ans** et **5–8 ans** retiennent les activités dont l’intervalle d’âge chevauche la tranche choisie, bornes incluses. Ils ne représentent pas un profil enfant enregistré.
- Chaque carte affiche le nom, un aperçu, l’âge, la durée et une ambiance lorsqu’elle existe. Un appui ouvre le détail : étapes ordonnées, matériel, note, variantes si présentes et lien vers la source.
- Chargement, erreur avec nouvelle tentative, catalogue vide et recherche sans résultat ont des états distincts.

Le modèle d’une activité prévoit aussi un minuteur optionnel par étape, une image et un lien de document. Tous ces champs ne disposent pas encore d’un usage dédié dans l’interface ; le catalogue actuel utilise le lien de document comme source.

## Navigation

Les quatre onglets sont **Idée**, **Fiches**, **Favoris** et **Paramètres**. Idée est l’onglet initial. Chaque onglet conserve sa pile de navigation ; retoucher l’onglet actif ne réinitialise pas les brouillons.

Retour depuis un détail revient aux fiches ; depuis une gestion, à Paramètres. À la racine d’un autre onglet, Retour revient à Idée. À la racine Idée, Android gère la sortie. La navigation passe d’une barre basse à un rail selon l’espace disponible.

## Gestion du catalogue en debug

**Paramètres → Gérer les tags / Gérer les matériels** permet de rechercher, créer, modifier et supprimer. Les tags sont filtrables par groupe et regroupés dans des sections repliables, ouvertes par défaut. Les compteurs reflètent les associations réelles ; pour un lieu parent, les activités des enfants sont incluses sans doublon.

| Règle | Comportement |
|---|---|
| Libellé | Obligatoire, espaces de début/fin retirés |
| Doublons | Refusés sans distinction de casse ; par groupe pour les tags |
| Groupes | Type, Lieu, Occasion, Saison, Moment, Participants, Ambiance |
| Hiérarchie | Lieux uniquement, un parent racine et un niveau d’enfants |
| Renommage | Identifiant et associations conservés |
| Suppression | Confirmation ; refus si le tag a des enfants ou si l’élément est associé à une activité |

Ces écrans et les mutations de gestion sont désactivés en release.

### Réimporter sans désinstaller

Le JSON embarqué est importé au démarrage avant l’affichage de la navigation. Le marqueur d’import empêche de rejouer une version déjà installée : les modifications et suppressions sont conservées lors des lancements suivants.

**Paramètres → Remplacer le catalogue**, en debug, demande confirmation puis restaure exactement le JSON dans une transaction. Les activités, tags et matériels ajoutés dans l’app sont supprimés et les éléments du catalogue sont restaurés. Les favoris et avis des activités conservées gardent leurs références ; ceux des activités supprimées sont effacés. Un état de progression puis un succès ou une erreur est affiché.

## Règles métier et intentions éditoriales

Le moteur de filtrage existe dans le code, mais n’est pas encore exposé comme parcours Idée : âge inclus dans la plage, durée maximale, matériel requis disponible, **OU dans un groupe de tags et ET entre groupes**. Une fiche sans tag dans un groupe n’impose aucune contrainte sur ce groupe ; un lieu parent sélectionné inclut ses enfants.

Les consignes éditoriales sont un vocabulaire contrôlé, un tag de type et au moins un tag d’ambiance par fiche, au plus huit tags par fiche et un nouveau tag utile à au moins cinq fiches. Ce sont des objectifs de contenu, pas des validations toutes imposées par l’outil debug. Les raccourcis « Il pleut », « On part en voyage » et le choix guidé temps/lieu/besoin restent des intentions produit.
