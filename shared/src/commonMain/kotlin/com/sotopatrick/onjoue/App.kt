package com.sotopatrick.onjoue

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sotopatrick.onjoue.designsystem.OnJoueTheme
import com.sotopatrick.onjoue.designsystem.components.DsBackground
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.navigation.AppNavigation
import com.sotopatrick.onjoue.presentation.settings.CatalogResetViewModel
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel

@Composable
fun App(
    createManagement: (() -> ManagementViewModel)? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    createStartup: (() -> StartupViewModel)? = null,
    createActivitySheets: (() -> ActivitySheetsViewModel)? = null,
    createActivityDetails: ((String) -> ActivityDetailsViewModel)? = null,
    createCatalogReset: (() -> CatalogResetViewModel)? = null,
    modifier: Modifier = Modifier,
) {
    val navigation: @Composable () -> Unit = {
        AppNavigation(createManagement, createActivitySheets, createActivityDetails, createCatalogReset, modifier)
    }
    OnJoueTheme(darkTheme) {
        if (createStartup == null) {
            navigation()
        } else {
            val startup = viewModel { createStartup() }
            val state by startup.state.collectAsStateWithLifecycle()
            if (state.ready) {
                navigation()
            } else {
                DsBackground(modifier) {
                    Column(
                        Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.XXLarge),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Large, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (state.loading) DsProgressIndicator()
                        state.error?.let {
                            DsText(it.content, tone = TextTone.Error)
                            DsTextButton("Réessayer", onClick = startup::load)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Phone", widthDp = 390, heightDp = 844)
@Preview(name = "Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun AppPreview() {
    App(darkTheme = false)
}

@Preview(name = "Phone dark", widthDp = 390, heightDp = 844)
@Preview(name = "Tablet dark", widthDp = 1024, heightDp = 768)
@Composable
private fun AppDarkPreview() {
    App(darkTheme = true)
}
