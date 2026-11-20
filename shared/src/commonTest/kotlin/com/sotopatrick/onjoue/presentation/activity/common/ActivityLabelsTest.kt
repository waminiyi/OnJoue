package com.sotopatrick.onjoue.presentation.activity.common

import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.AgeRange
import com.sotopatrick.onjoue.data.repositories.sampleActivity
import kotlin.test.Test
import kotlin.test.assertEquals

class ActivityLabelsTest {
    @Test
    fun ageRangeUsesSharedYearsUnitWhenBothBoundsAreWholeYears() {
        val activity = sampleActivity().copy(age = AgeRange(AgeMonths(36), AgeMonths(60)))

        assertEquals("3–5 ans", ageRangeLabel(activity))
    }

    @Test
    fun ageRangePreservesMonthPrecision() {
        val activity = sampleActivity().copy(age = AgeRange(AgeMonths(37), AgeMonths(60)))

        assertEquals("3 ans 1 mois–5 ans", ageRangeLabel(activity))
    }
}
