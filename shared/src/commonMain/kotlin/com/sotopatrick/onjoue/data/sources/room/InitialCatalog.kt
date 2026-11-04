package com.sotopatrick.onjoue.data.sources.room

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.ValidationReason
import com.sotopatrick.onjoue.data.models.tag.TagGroup

internal object InitialCatalog {
    private data class SeedTag(val key: String, val label: String, val group: TagGroup, val parentKey: String? = null)

    private val tags = buildList {
        fun group(group: TagGroup, vararg entries: Pair<String, String>) {
            entries.forEach { (key, label) -> add(SeedTag(key, label, group)) }
        }
        group(TagGroup.TYPE, "game" to "Jeu", "craft" to "Bricolage", "drawing" to "Dessin",
            "story" to "Conte", "music" to "Musique", "exploration" to "Exploration", "cooking" to "Cuisine")
        group(TagGroup.LOCATION, "inside" to "Dedans", "outside" to "Dehors", "travel" to "En déplacement")
        listOf(
            Triple("living-room", "Salon", "inside"), Triple("bedroom", "Chambre", "inside"),
            Triple("kitchen", "Cuisine", "inside"), Triple("small-space", "Petit espace", "inside"),
            Triple("garden", "Jardin", "outside"), Triple("park", "Parc", "outside"),
            Triple("forest", "Forêt", "outside"), Triple("playground", "Aire de jeux", "outside"),
            Triple("car", "Voiture", "travel"), Triple("train", "Train", "travel"),
        ).forEach { (key, label, parent) -> add(SeedTag(key, label, TagGroup.LOCATION, parent)) }
        group(TagGroup.OCCASION, "rainy-day" to "Jour de pluie", "birthday" to "Anniversaire",
            "holidays" to "Vacances", "waiting" to "Temps d'attente")
        group(TagGroup.SEASON, "spring" to "Printemps", "summer" to "Été", "autumn" to "Automne", "winter" to "Hiver")
        group(TagGroup.TIME, "morning" to "Matin", "afternoon" to "Après-midi", "evening" to "Soir", "bedtime" to "Avant le coucher")
        group(TagGroup.PARTICIPANTS, "solo" to "Seul", "pair" to "À deux", "family" to "En famille", "group" to "En groupe")
        group(TagGroup.MOOD, "active" to "Se dépenser", "calm" to "Se calmer", "focus" to "Se concentrer",
            "creative" to "Créer", "cooperate" to "Coopérer")
    }

    private val equipment = listOf(
        "ball" to "Ballon", "cushions" to "Coussins", "soft-toys" to "Peluches", "rope" to "Corde",
        "chairs" to "Chaises", "chalk" to "Craie", "tape" to "Ruban adhésif", "paper" to "Papier",
        "colored-pencils" to "Crayons de couleur", "markers" to "Feutres", "scissors" to "Ciseaux",
        "glue" to "Colle", "cardboard" to "Carton", "clay" to "Pâte à modeler", "paint" to "Peinture",
        "brushes" to "Pinceaux", "books" to "Livres", "blocks" to "Cubes de construction",
        "hoops" to "Cerceaux", "blanket" to "Couverture", "containers" to "Récipients",
        "spoons" to "Cuillères", "clothespins" to "Pinces à linge", "bubbles" to "Bulles de savon",
    )

    fun insert(connection: SQLiteConnection) {
        val ids = mutableMapOf<String, String>()
        for (tag in tags) {
            val existing = connection.prepare("SELECT id FROM tags WHERE groupName = ? AND normalizedLabel = ?").use {
                it.bindText(1, tag.group.name)
                it.bindText(2, tag.label.lowercase())
                if (it.step()) it.getText(0) else null
            }
            if (existing != null) {
                ids[tag.key] = existing
                continue
            }
            val parent = tag.parentKey?.let { ids.getValue(it) }
            if (parent != null) {
                connection.prepare("SELECT parent FROM tags WHERE id = ?").use {
                    it.bindText(1, parent)
                    check(it.step())
                    if (!it.isNull(0)) throw AppException.Validation(ValidationReason.INVALID_HIERARCHY)
                }
            }
            val id = "catalog-tag-${tag.key}"
            connection.prepare("INSERT INTO tags(id, label, groupName, parent, normalizedLabel) VALUES(?, ?, ?, ?, ?)").use {
                it.bindText(1, id)
                it.bindText(2, tag.label)
                it.bindText(3, tag.group.name)
                if (parent == null) it.bindNull(4) else it.bindText(4, parent)
                it.bindText(5, tag.label.lowercase())
                it.step()
            }
            ids[tag.key] = id
        }
        for ((key, label) in equipment) {
            val exists = connection.prepare("SELECT 1 FROM equipment WHERE normalizedLabel = ?").use {
                it.bindText(1, label.lowercase())
                it.step()
            }
            if (!exists) {
                connection.prepare("INSERT INTO equipment(id, label, normalizedLabel) VALUES(?, ?, ?)").use {
                    it.bindText(1, "catalog-equipment-$key")
                    it.bindText(2, label)
                    it.bindText(3, label.lowercase())
                    it.step()
                }
            }
        }
    }
}

internal object InitialCatalogCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        InitialCatalog.insert(connection)
    }
}
