package com.sotopatrick.onjoue.presentation.activity

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.AgeMonths

internal fun ageRangeLabel(activity: Activity): String =
    "${ageLabel(activity.age.min)}–${ageLabel(activity.age.max)}"

private fun ageLabel(age: AgeMonths): String {
    val years = age.value / 12
    val months = age.value % 12
    val yearLabel = if (years == 1) "1 an" else "$years ans"
    return when {
        years == 0 -> "$months mois"
        months == 0 -> yearLabel
        else -> "$yearLabel $months mois"
    }
}

private val ActivityEmojis = listOf(
    listOf("bulle") to "🫧",
    listOf("plage", "mer") to "🌊",
    listOf("tapis") to "🛸",
    listOf("bas") to "🧦",
    listOf("nouille") to "🤸",
    listOf("défilé") to "🎭",
    listOf("parcours", "slalom") to "🏃",
    listOf("olympique") to "🥇",
    listOf("randonnée") to "🥾",
    listOf("limbo") to "💃",
    listOf("cirque") to "🎪",
    listOf("ballon") to "⚽",
    listOf("loup") to "🐺",
    listOf("nage") to "🐟",
    listOf("soleil") to "☀️",
    listOf("soccer") to "🎳",
    listOf("mouchoir") to "🪁",
    listOf("chèvre") to "🐐",
    listOf("marelle") to "🟦",
    listOf("escrime") to "🤺",
    listOf("lac") to "🦩",
    listOf("yoga") to "🧘",
    listOf("danse", "statue") to "🎵",
    listOf("twister", "géométriques") to "🔺",
    listOf("mini-banquise") to "🐧",
    listOf("jardin") to "🌷",
    listOf("pompons") to "🧶",
    listOf("construction", "bâtons") to "🧩",
    listOf("mémoire") to "🖼️",
    listOf("jouet") to "🟠",
    listOf("touche-à-tout") to "🖐️",
    listOf("classée") to "🫙",
    listOf("lecture") to "📖",
)

internal fun activityEmoji(activity: Activity): String {
    val name = activity.name.lowercase()
    return ActivityEmojis.firstOrNull { (keywords, _) -> keywords.any { it in name } }?.second ?: "✨"
}
