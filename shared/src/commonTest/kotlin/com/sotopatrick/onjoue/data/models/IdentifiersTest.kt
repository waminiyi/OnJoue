package com.sotopatrick.onjoue.data.models

import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.models.review.ReviewId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IdentifiersTest {
    @Test
    fun validIdentifiersPreserveValue() {
        assertEquals("avis-1", ReviewId("avis-1").id)
        assertEquals(ReviewId("avis-1"), ReviewId("avis-1"))
        assertEquals("favori-1", FavoriteId("favori-1").id)
        assertEquals(FavoriteId("favori-1"), FavoriteId("favori-1"))
    }

    @Test
    fun blankIdentifiersAreRejected() {
        for (value in listOf("", " ", "\t\n")) {
            assertFailsWith<IllegalArgumentException> { ReviewId(value) }
            assertFailsWith<IllegalArgumentException> { FavoriteId(value) }
        }
    }
}
