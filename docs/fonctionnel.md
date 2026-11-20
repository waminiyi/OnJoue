# Fonctionnement d’OnJoue

OnJoue aide à trouver une activité à partager avec un enfant. L’objectif produit est de proposer rapidement une idée selon l’âge, le temps disponible, le lieu et le besoin du moment. Le parcours de recommandation n’est pas encore disponible ; la consultation des idées l’est.

## Périmètre actuel

| Fonction | État |
|---|---|
| Idées : catalogue, recherche, filtres rapides et détail | Disponible en debug et release |
| Gestion éditoriale et remplacement du catalogue | Non disponibles dans l’application |
| Onglets Aujourd’hui et Favoris ; gestion des activités | Écrans explicitement indisponibles |
| Favoris depuis les cartes d’idées | Ajout/retrait persistés localement, en debug et release |
| Avis ; création/modification d’activités | Modèles et repositories présents, sans parcours UI dédié |
| Profil enfant, matériel du foyer, préférences et synchronisation | Non implémentés |

Les fiches du catalogue sont conservées sur l’appareil. Les images d’activité peuvent être chargées depuis Firebase Storage; l’application utilise alors une illustration générique embarquée si l’image est absente ou indisponible.

## Consulter les idées

- Le catalogue livré contient **88 activités, 41 tags et 57 matériels**, issus de quatre sources Naître et grandir : [47 activités pour les bébés de 0 à 12 mois](https://naitreetgrandir.com/fr/etape/0_12_mois/fiches-activites/jeux/), [31 activités pour bouger](https://naitreetgrandir.com/fr/etape/5-8-ans/jouer-bouger/31-activites-bouger-avec-enfant/), [Acrobaties géométriques](https://naitreetgrandir.com/fr/etape/3-5-ans/fiches-activites/jeux/acrobaties-geometriques/) et [Jeux maison](https://naitreetgrandir.com/fr/etape/3-5-ans/fiches-activites/jeux/jeux-maison/). Les descriptions et étapes sont reformulées.
- Dans **Idées**, la recherche porte sur le nom, la description, les tags et les matériels, sans distinction de casse et en ignorant les espaces de bord.
- Les filtres **Tous les âges**, **1–3 ans**, **3–5 ans** et **5–8 ans** retiennent les activités dont l’intervalle d’âge chevauche la tranche choisie, bornes incluses. Ils ne représentent pas un profil enfant enregistré.
- Les raccourcis **Dedans** et **Dehors** sont exclusifs entre eux ; retoucher le lieu sélectionné retire ce critère. Un lieu racine inclut ses enfants ; une activité sans contrainte de lieu reste proposée.
- Le raccourci **15 min ou moins** inclut les idées de 15 minutes. Âge, lieu, durée et recherche se combinent. L’entrée des filtres avancés est désactivée et annoncée « Bientôt disponible » ; aucune bottom sheet n’est encore proposée.
- Le listing présente un fond mint et des cartes claires, adaptées au thème sombre. Chaque carte affiche l’image de l’activité, ou une illustration générique embarquée si l’image est absente ou ne peut pas être chargée, puis le titre, l’âge, la durée et une ambiance lorsqu’elle existe, sans pastilles de fond. Un appui ouvre le détail, qui reprend l’image avec le même fallback, puis la description, les étapes ordonnées, le matériel, la note, les variantes si présentes et le lien vers la source.
- Le cœur vide/plein permet d’ajouter ou de retirer un favori sans ouvrir le détail. Les favoris sont enregistrés sur l’appareil ; leur chargement et leurs erreurs n’empêchent pas la consultation du catalogue. Les actions sont désactivées tant que leur état n’est pas fiable ou qu’une écriture est en cours.
- Le chargement affiche **« Les idées arrivent… »**. Une erreur de chargement propose **Réessayer** ; si des idées étaient déjà chargées, elles restent consultables.
- Le catalogue réellement vide invite à revenir plus tard et à réessayer. Une recherche sans résultat invite à élargir les critères ; **Retirer les filtres** efface tous les filtres rapides et la recherche.

Le modèle d’une activité prévoit aussi un minuteur optionnel par étape, une image et un lien de document. Les images sont référencées par URL HTTPS Firebase Storage; leur absence ou échec de chargement ne bloque pas la consultation. Le catalogue utilise le lien de document comme source.

## Navigation

Les quatre onglets sont **Aujourd’hui**, **Idées**, **Favoris** et **Paramètres**. Aujourd’hui est l’onglet initial. Chaque onglet conserve sa pile de navigation ; retoucher l’onglet actif ne réinitialise pas les brouillons. Recherche, filtres et défilement du catalogue sont conservés lors d’un changement d’onglet et de la restauration de l’écran.

Retour depuis un détail revient aux idées. À la racine d’un autre onglet, Retour revient à Aujourd’hui. À la racine Aujourd’hui, Android gère la sortie. La navigation passe d’une barre basse à un rail selon l’espace disponible.

## Catalogue local

L’application ne propose plus d’outils éditoriaux, y compris en debug. Les opérations de persistance nécessaires à l’import restent disponibles en interne.

| Règle | Comportement |
|---|---|
| Libellé | Obligatoire, espaces de début/fin retirés |
| Doublons | Refusés sans distinction de casse ; par groupe pour les tags |
| Groupes | Type, Lieu, Occasion, Saison, Moment, Participants, Ambiance |
| Hiérarchie | Lieux uniquement, un parent racine et un niveau d’enfants |
| Renommage | Identifiant et associations conservés |
| Suppression | Confirmation ; refus si le tag a des enfants ou si l’élément est associé à une activité |

Le JSON embarqué est importé au démarrage avant l’affichage de la navigation. Le marqueur d’import empêche de rejouer une version déjà installée : les modifications et suppressions sont conservées lors des lancements suivants.

## Règles métier et intentions éditoriales

Le moteur de filtrage existe dans le code, mais n’est pas encore exposé comme parcours guidé Aujourd’hui : âge inclus dans la plage, durée maximale, matériel requis disponible, **OU dans un groupe de tags et ET entre groupes**. Une activité sans tag dans un groupe n’impose aucune contrainte sur ce groupe ; un lieu parent sélectionné inclut ses enfants. Le listing utilise des tranches d’âge par chevauchement, et non un âge d’enfant précis.

Les consignes éditoriales sont un vocabulaire contrôlé, un tag de type et au moins un tag d’ambiance par fiche, au plus huit tags par fiche et un nouveau tag utile à au moins cinq fiches. Ce sont des objectifs de contenu, pas des validations toutes imposées par l’import actuel. Les raccourcis « Il pleut », « On part en voyage » et le choix guidé temps/lieu/besoin restent des intentions produit.
