package com.sotopatrick.onjoue.presentation.activity.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing

@Composable
internal fun IdeaStatus(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier.padding(Spacing.XXLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Large, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) DsProgressIndicator()
        DsText(title, role = TextRole.HeadlineSmall, textAlign = TextAlign.Center)
        DsText(description, role = TextRole.BodyLarge, tone = TextTone.Secondary, textAlign = TextAlign.Center)
        if (actionLabel != null) DsButton(actionLabel, onClick = onAction)
    }
}
