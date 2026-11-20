package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.designsystem.tokens.Dimens

/** Emphasis of the confirmation action of a dialog. */
public enum class ConfirmStyle {
    /** Filled button for a significant, intended action. */
    Primary,

    /** Error-colored text button for a deletion. */
    Destructive,
}

/**
 * Confirmation dialog stating the consequence of an action, with confirm and cancel actions.
 * [error] is displayed under the message when the action failed.
 */
@Composable
public fun DsConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Annuler",
    confirmStyle: ConfirmStyle = ConfirmStyle.Primary,
    enabled: Boolean = true,
    dismissEnabled: Boolean = true,
    error: String? = null,
) {
    AlertDialog(
        onDismissRequest = { if (dismissEnabled) onDismiss() },
        modifier = modifier,
        title = { Text(title) },
        text = {
            if (error == null) {
                Text(message)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                    Text(message)
                    DsText(error, tone = TextTone.Error)
                }
            }
        },
        confirmButton = {
            when (confirmStyle) {
                ConfirmStyle.Primary -> DsButton(confirmLabel, onClick = onConfirm, enabled = enabled)
                ConfirmStyle.Destructive -> DsTextButton(
                    confirmLabel,
                    onClick = onConfirm,
                    enabled = enabled,
                    tone = ActionTone.Destructive,
                )
            }
        },
        dismissButton = { DsTextButton(dismissLabel, onClick = onDismiss, enabled = dismissEnabled) },
    )
}

/** Scrollable dialog hosting a form, sized for phones and tablets and kept above the keyboard. */
@Composable
public fun DsFormDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = modifier.widthIn(max = Dimens.DialogMaxWidth).imePadding(),
        ) {
            Column(
                Modifier.heightIn(max = Dimens.DialogMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.ExtraLarge),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
            ) {
                DsText(title, role = TextRole.TitleLarge)
                content()
            }
        }
    }
}

/** Modal bottom sheet for filter menus and quick actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun DsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        content = content,
    )
}

