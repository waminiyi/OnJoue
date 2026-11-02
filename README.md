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
