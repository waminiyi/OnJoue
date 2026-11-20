package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/** Image with a local fallback; sizing and clipping belong to the caller's layout. */
@Composable
public fun DsImage(
    url: String?,
    fallback: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    AsyncImage(
        model = url?.takeIf(String::isNotBlank),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = fallback,
        error = fallback,
        fallback = fallback,
    )
}
