package com.sotopatrick.onjoue

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.navigation.AppNavigation
import com.sotopatrick.onjoue.presentation.theme.OnJoueTheme
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel

@Composable
fun App(
    createManagement: (() -> ManagementViewModel)? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    createStartup: (() -> StartupViewModel)? = null,
) {
    OnJoueTheme(darkTheme) {
        if (createStartup == null) {
            AppNavigation(createManagement)
        } else {
            val startup = viewModel { createStartup() }
            val state by startup.state.collectAsStateWithLifecycle()
            if (state.ready) {
                AppNavigation(createManagement)
            } else {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (state.loading) CircularProgressIndicator()
                        state.error?.let {
                            Text(it.content, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = startup::load) { Text("Réessayer") }
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
fun AppPreview() {
    App(darkTheme = false)
}

@Preview(name = "Phone dark", widthDp = 390, heightDp = 844)
@Preview(name = "Tablet dark", widthDp = 1024, heightDp = 768)
@Composable
fun AppDarkPreview() {
    App(darkTheme = true)
}
