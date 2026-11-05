package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.designsystem.tokens.Dimens

/** Back action of a [DsScaffold] top bar. */
public class DsBackAction(
    public val icon: Painter,
    public val contentDescription: String,
    public val enabled: Boolean,
    public val onClick: () -> Unit,
)

/**
 * Secondary screen with a top bar (title and back action) and an optional floating action.
 * Window insets are handled by the navigation container.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
public fun DsScaffold(
    title: String,
    modifier: Modifier = Modifier,
    backAction: DsBackAction? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    backAction?.let { back ->
                        IconButton(onClick = back.onClick, enabled = back.enabled) {
                            Icon(back.icon, contentDescription = back.contentDescription)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

/**
 * Adaptive grid of cards: as many columns of at least 560 dp as fit, with list margins
 * and bottom room for a floating action button.
 */
@Composable
public fun DsAdaptiveGrid(
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(Dimens.GridMinColumnWidth),
        modifier = modifier.padding(horizontal = Spacing.ExtraLarge),
        contentPadding = PaddingValues(top = Spacing.Medium, bottom = Dimens.FabClearance),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Large),
        content = content,
    )
}

/**
 * Full-width header toggling a collapsible group. Exposes its expanded state to accessibility services.
 */
@Composable
public fun DsCollapsibleHeader(
    label: String,
    expanded: Boolean,
    expandIcon: Painter,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth().semantics {
            stateDescription = if (expanded) "Déplié" else "Replié"
        },
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        contentPadding = PaddingValues(vertical = Spacing.Medium),
    ) {
        DsText(label, modifier = Modifier.weight(1f), role = TextRole.LabelLarge)
        Icon(
            expandIcon,
            contentDescription = null,
            modifier = Modifier.rotate(if (expanded) ExpandedRotation else 0f),
        )
    }
}

private const val ExpandedRotation = 180f
