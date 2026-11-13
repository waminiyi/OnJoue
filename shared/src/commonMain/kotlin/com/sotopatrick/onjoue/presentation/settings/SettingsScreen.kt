package com.sotopatrick.onjoue.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(Spacing.XXLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        DsText("Paramètres", role = TextRole.HeadlineMedium)
        DsText("Aucun paramètre disponible pour le moment.", tone = TextTone.Secondary)
    }
}
