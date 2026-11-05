package com.sotopatrick.onjoue.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing

@Composable
fun UnavailableScreen(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    Box(modifier.fillMaxSize().padding(Spacing.XXLarge), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
        ) {
            DsText(title, role = TextRole.HeadlineMedium, textAlign = TextAlign.Center)
            DsText(
                "Fonctionnalité non disponible pour le moment.",
                role = TextRole.BodyLarge,
                tone = TextTone.Secondary,
                textAlign = TextAlign.Center,
            )
            onBack?.let { DsTextButton("Retour aux paramètres", onClick = it) }
        }
    }
}
