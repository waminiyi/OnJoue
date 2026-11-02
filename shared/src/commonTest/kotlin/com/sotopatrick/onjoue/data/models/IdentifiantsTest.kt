package com.sotopatrick.onjoue.data.models

import com.sotopatrick.onjoue.data.models.avis.IdAvis
import com.sotopatrick.onjoue.data.models.favori.IdFavori
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IdentifiantsTest {
    @Test
    fun identifiantsValidesConserventLeurValeur() {
        assertEquals("avis-1", IdAvis("avis-1").id)
        assertEquals(IdAvis("avis-1"), IdAvis("avis-1"))
        assertEquals("favori-1", IdFavori("favori-1").id)
        assertEquals(IdFavori("favori-1"), IdFavori("favori-1"))
    }

    @Test
    fun identifiantsVidesOuBlancsRefuses() {
        for (valeur in listOf("", " ", "\t\n")) {
            assertFailsWith<IllegalArgumentException> { IdAvis(valeur) }
            assertFailsWith<IllegalArgumentException> { IdFavori(valeur) }
        }
    }
}
