package com.sotopatrick.onjoue.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.designsystem.components.DsActionCard
import com.sotopatrick.onjoue.designsystem.components.DsConfirmDialog
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing

@Composable
fun SettingsScreen(
    managementEnabled: Boolean,
    onManageTags: () -> Unit,
    onManageEquipment: () -> Unit,
    onManageActivities: () -> Unit,
    modifier: Modifier = Modifier,
    catalogResetState: CatalogResetState? = null,
    onResetCatalog: (() -> Unit)? = null,
) {
    var confirmReset by remember { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.XXLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        DsText("Paramètres", role = TextRole.HeadlineMedium)
        if (managementEnabled) {
            SectionTitle("Gestion du catalogue")
            DsText("Réservée au mode debug", role = TextRole.BodyMedium, tone = TextTone.Secondary)
            ManagementSetting("Gérer les tags", "Groupes et hiérarchie des lieux", onManageTags)
            ManagementSetting("Gérer les matériels", "Catalogue des matériels", onManageEquipment)
            ManagementSetting("Gérer les activités", "Non disponible pour le moment", onManageActivities)
            if (onResetCatalog != null && catalogResetState != null) {
                CatalogResetSection(catalogResetState, onRequestReset = { confirmReset = true })
            }
        } else {
            DsText("Aucun paramètre disponible pour le moment.", tone = TextTone.Secondary)
        }
    }
    if (confirmReset && onResetCatalog != null) {
        DsConfirmDialog(
            title = "Remplacer tout le catalogue ?",
            message = "Cette action supprime les tags, matériels et activités ajoutés dans l’application, " +
                "puis recharge exactement le catalogue initial. Les favoris et avis des activités " +
                "supprimées seront également effacés.",
            confirmLabel = "Remplacer",
            onConfirm = {
                confirmReset = false
                onResetCatalog()
            },
            onDismiss = { confirmReset = false },
            enabled = catalogResetState?.busy != true,
        )
    }
}

@Composable
private fun CatalogResetSection(state: CatalogResetState, onRequestReset: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        SectionTitle("Outils de développement")
        ManagementSetting(
            "Remplacer le catalogue",
            "Recharge le catalogue fourni avec l’application",
            onRequestReset,
        )
        if (state.busy) {
            DsProgressIndicator()
            DsText("Remplacement du catalogue…")
        }
        state.message?.let { message ->
            DsText(message.content, tone = if (state.succeeded) TextTone.Primary else TextTone.Error)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    DsText(title, role = TextRole.TitleMedium, modifier = Modifier.padding(top = Spacing.Medium))
}

@Composable
private fun ManagementSetting(title: String, description: String, onClick: () -> Unit) {
    DsActionCard(title, description, onClick, modifier = Modifier.fillMaxWidth())
}
