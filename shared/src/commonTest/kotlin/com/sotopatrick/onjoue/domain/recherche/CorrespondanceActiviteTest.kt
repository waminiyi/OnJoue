package com.sotopatrick.onjoue.domain.recherche

import com.sotopatrick.onjoue.data.models.catalogues.CatalogueTags
import com.sotopatrick.onjoue.data.models.activite.Activite
import com.sotopatrick.onjoue.data.models.activite.AgeMois
import com.sotopatrick.onjoue.data.models.activite.DureeMinutes
import com.sotopatrick.onjoue.data.models.activite.IdActivite
import com.sotopatrick.onjoue.data.models.activite.PlageAge
import com.sotopatrick.onjoue.data.models.materiel.MaterielId
import com.sotopatrick.onjoue.data.models.recherche.FiltreIdee
import com.sotopatrick.onjoue.data.models.recherche.correspond
import com.sotopatrick.onjoue.data.models.tag.GroupeTag
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CorrespondanceActiviteTest {
    private val dedans = Tag(TagId("dedans"), GroupeTag.LIEU, "Dedans")
    private val salon = Tag(TagId("salon"), GroupeTag.LIEU, "Salon", parent = dedans.id)
    private val dehors = Tag(TagId("dehors"), GroupeTag.LIEU, "Dehors")
    private val calme = Tag(TagId("calme"), GroupeTag.AMBIANCE, "Se calmer")
    private val actif = Tag(TagId("actif"), GroupeTag.AMBIANCE, "Se dépenser")
    private val catalogue = CatalogueTags(listOf(dedans, salon, dehors, calme, actif))
    private val ballon = MaterielId("ballon")
    private val activite = Activite(
        id = IdActivite("jeu"),
        nom = "Jeu de ballon",
        description = "Faire rouler le ballon.",
        age = PlageAge(AgeMois(24), AgeMois(60)),
        duree = DureeMinutes(10),
        materiel = setOf(ballon),
        tags = setOf(salon.id, calme.id),
    )
    private val filtre = FiltreIdee(ageEnfant = AgeMois(36))

    @Test
    fun valeursInvalidesRefusees() {
        assertFailsWith<IllegalArgumentException> { IdActivite(" ") }
        assertFailsWith<IllegalArgumentException> { TagId("") }
        assertFailsWith<IllegalArgumentException> { MaterielId("") }
        assertFailsWith<IllegalArgumentException> { AgeMois(-1) }
        assertFailsWith<IllegalArgumentException> { DureeMinutes(0) }
        assertFailsWith<IllegalArgumentException> { DureeMinutes(-1) }
        assertFailsWith<IllegalArgumentException> { PlageAge(AgeMois(60), AgeMois(24)) }
    }

    @Test
    fun ageInclusifEtStatutPublie() {
        assertTrue(activite.correspond(filtre.copy(ageEnfant = AgeMois(24)), catalogue))
        assertTrue(activite.correspond(filtre.copy(ageEnfant = AgeMois(60)), catalogue))
        assertFalse(activite.correspond(filtre.copy(ageEnfant = AgeMois(23)), catalogue))
        assertFalse(activite.correspond(filtre.copy(ageEnfant = AgeMois(61)), catalogue))
        assertFalse(activite.copy(statut = Statut.RETIRE).correspond(filtre, catalogue))
    }

    @Test
    fun dureeMaxInclusiveEtOptionnelle() {
        assertTrue(activite.correspond(filtre, catalogue))
        assertTrue(activite.correspond(filtre.copy(dureeMax = DureeMinutes(10)), catalogue))
        assertFalse(activite.correspond(filtre.copy(dureeMax = DureeMinutes(9)), catalogue))
    }

    @Test
    fun materielNullIgnoreLeFiltreEtEnsembleVideExclutLeMaterielRequis() {
        assertTrue(activite.correspond(filtre, catalogue))
        assertFalse(activite.correspond(filtre.copy(materielDisponible = emptySet()), catalogue))
        assertTrue(activite.correspond(filtre.copy(materielDisponible = setOf(ballon)), catalogue))
        assertTrue(
            activite.copy(materiel = emptySet())
                .correspond(filtre.copy(materielDisponible = emptySet()), catalogue)
        )
    }

    @Test
    fun catalogueInclutLeParentEtSesEnfantsDirects() {
        assertEquals(salon, catalogue[salon.id])
        assertEquals(null, catalogue[TagId("inconnu")])
        assertEquals(setOf(dedans.id, salon.id), catalogue.etendu(setOf(dedans.id)))
        assertEquals(setOf(salon.id), catalogue.etendu(setOf(salon.id)))
    }

    @Test
    fun tagsOuDansUnGroupeEtEntreGroupes() {
        val choix = filtre.copy(
            tagsChoisis = mapOf(
                GroupeTag.LIEU to setOf(dehors.id, dedans.id),
                GroupeTag.AMBIANCE to setOf(calme.id),
            )
        )
        assertTrue(activite.correspond(choix, catalogue))
        assertFalse(activite.copy(tags = setOf(salon.id, actif.id)).correspond(choix, catalogue))
        assertFalse(
            activite.correspond(
                filtre.copy(tagsChoisis = mapOf(GroupeTag.LIEU to setOf(dehors.id))),
                catalogue,
            )
        )
    }

    @Test
    fun groupeAbsentOuSelectionVideNeContraintPasLaRecherche() {
        val choix = filtre.copy(tagsChoisis = mapOf(GroupeTag.LIEU to setOf(dehors.id)))
        assertTrue(activite.copy(tags = setOf(calme.id)).correspond(choix, catalogue))
        assertTrue(
            activite.correspond(
                filtre.copy(tagsChoisis = mapOf(GroupeTag.LIEU to emptySet())),
                catalogue,
            )
        )
    }
}
