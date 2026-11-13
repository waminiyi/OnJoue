# Contrat envisagé du catalogue distant

Ce document décrit le contrat à implémenter entre le client et une future publication
éditoriale. **Il n’existe pas encore de serveur ni de synchronisation dans l’application.**
Le backoffice web et son infrastructure sont hors du périmètre actuel.

## Instantané

Le client récupérera en HTTPS un document JSON complet, en lecture seule et sans compte
utilisateur. La réponse doit représenter une seule publication cohérente : les brouillons
et les publications partielles ne doivent jamais être exposés au client.

```json
{
  "formatVersion": 1,
  "revision": 1,
  "tags": [
    { "id": "tag-jeu", "label": "Jeu", "group": "TYPE", "parentId": null }
  ],
  "equipment": [
    { "id": "materiel-ballon", "label": "Ballon", "iconId": "ball" }
  ],
  "activities": [
    {
      "id": "activite-ballon",
      "name": "Jouer au ballon",
      "description": "Un jeu à partager.",
      "note": null,
      "minAgeMonths": 24,
      "maxAgeMonths": 96,
      "durationMinutes": 10,
      "steps": ["Faire rouler le ballon."],
      "variants": [],
      "tagIds": ["tag-jeu"],
      "equipmentIds": ["materiel-ballon"],
      "iconId": "ball",
      "document": null
    }
  ]
}
```

`formatVersion` identifie le format et vaut `1` pour ce contrat. `revision` est un
entier positif, strictement croissant à chaque publication. Une même révision identifie
toujours exactement le même contenu ; le catalogue embarqué doit provenir du même flux
de révisions que les publications distantes. Revenir à un ancien contenu nécessite de
le republier avec une nouvelle révision.

Les identifiants sont non vides, uniques dans leur collection et stables d’une
publication à l’autre : ils ne doivent jamais être réutilisés pour un autre élément.
Une activité conserve son historique local tant que son identifiant est conservé. Les
icônes sont choisies par `iconId` dans une bibliothèque vectorielle embarquée ; un
identifiant inconnu doit produire une icône générique, pas le rejet de la publication.

Les sept groupes sont `TYPE`, `LOCATION`, `OCCASION`, `SEASON`, `TIME`, `PARTICIPANTS`
et `MOOD`. Seul un lieu peut avoir un parent, qui doit lui-même être un lieu racine.
Les libellés, identifiants et noms sont non vides. Les libellés sont comparés après
`trim()` et `lowercase()` Kotlin : l’unicité vaut par groupe de tags et pour tout le
catalogue des matériels. Les références doivent exister dans le même instantané et ne
pas être répétées. Les âges sont positifs ou nuls et ordonnés ; la durée est positive.
Les étapes et variantes ne peuvent pas contenir de texte vide.

Le client doit rejeter un format non pris en charge, une révision invalide, un JSON
malformé ou un instantané incohérent, sans modifier la base. Le décodeur limite le JSON
à 2 000 000 caractères et chaque collection principale à 10 000 éléments ; le futur
client réseau devra aussi limiter les octets reçus avant leur décodage. La configuration
du point de lecture ne doit contenir aucun secret.

## Application locale

Au premier démarrage, le catalogue embarqué est validé et importé avant l’accès aux
fiches, sans attendre le réseau. Ensuite, le client recherche une publication au
lancement et au retour au premier plan, sans bloquer la consultation. Si aucune adresse
distante n’est configurée, Paramètres indique que la synchronisation est indisponible.
Une erreur laisse le catalogue local disponible et permet de réessayer depuis Paramètres.

Une révision supérieure est appliquée en une transaction avec son marqueur. Une révision
identique ou inférieure ne doit pas écraser le catalogue local, y compris si des requêtes
se terminent dans le désordre. Une mise à jour de l’application ne doit pas remplacer
une publication distante plus récente par son catalogue embarqué.

L’instantané publié fait autorité : les activités absentes sont supprimées, ainsi que
leurs avis et favoris locaux. Les activités toujours présentes gardent leurs identifiants,
avis et favoris. Les tags et matériels absents sont retirés après mise à jour des
associations. Les anciens ajouts et modifications effectués en debug ne font pas
exception. Les collisions incompatibles doivent être signalées et annuler la transaction.

Le contrat ne prévoit pas la synchronisation des avis, des favoris ou d’autres données
personnelles. La future publication devra protéger ses opérations d’écriture et exposer
uniquement l’instantané publié en lecture ; ses choix de déploiement et d’authentification
restent à mettre en œuvre séparément.
