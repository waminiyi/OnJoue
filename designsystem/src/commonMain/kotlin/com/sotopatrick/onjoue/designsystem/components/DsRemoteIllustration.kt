package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import coil3.compose.AsyncImage
import com.sotopatrick.onjoue.designsystem.tokens.ComponentShapes
import com.sotopatrick.onjoue.designsystem.tokens.Dimens
import com.sotopatrick.onjoue.designsystem.tokens.HeroIllustrationSurfaceAlpha

/** Network image with a local illustration shown while loading or when loading fails. */
@Composable
public fun DsRemoteIllustration(
    imageUrl: String?,
    fallbackPainter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: IllustrationSize = IllustrationSize.Medium,
    style: IllustrationStyle = IllustrationStyle.Plain,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val background = when (size) {
        IllustrationSize.Medium -> MaterialTheme.colorScheme.primaryContainer
        IllustrationSize.Large,
        IllustrationSize.ExtraLarge -> MaterialTheme.colorScheme.surface.copy(
            alpha = HeroIllustrationSurfaceAlpha,
        )
    }
    val medium = size == IllustrationSize.Medium
    val boxSize = size.dimension()
    val shape = if (medium) ComponentShapes.Illustration else ComponentShapes.HeroIllustration
    Box(
        modifier
            .size(boxSize)
            .clip(shape)
            .then(if (style == IllustrationStyle.Tinted) Modifier.background(background) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl?.takeIf(String::isNotBlank),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            placeholder = fallbackPainter,
            error = fallbackPainter,
            fallback = fallbackPainter,
        )
    }
}

internal fun IllustrationSize.dimension() = when (this) {
    IllustrationSize.Medium -> Dimens.IllustrationSize
    IllustrationSize.Large -> Dimens.HeroIllustrationSize
    IllustrationSize.ExtraLarge -> Dimens.DetailHeroIllustrationSize
}
