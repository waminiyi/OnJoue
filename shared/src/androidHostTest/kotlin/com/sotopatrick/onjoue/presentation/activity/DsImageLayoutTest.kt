package com.sotopatrick.onjoue.presentation.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.sotopatrick.onjoue.designsystem.OnJoueTheme
import com.sotopatrick.onjoue.designsystem.components.DsCard
import com.sotopatrick.onjoue.designsystem.components.DsImage
import com.sotopatrick.onjoue.designsystem.components.dsCardImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DsImageLayoutTest {
    private val fallback = object : Painter() {
        override val intrinsicSize = Size(20f, 10f)
        override fun DrawScope.onDraw() {
            drawRect(Color.Blue)
        }
    }

    @Test
    fun missingImageUsesFallbackWithoutFixedSizingOrClipping() = runComposeUiTest {
        setContent {
            OnJoueTheme {
                DsImage(
                    url = null,
                    fallback = fallback,
                    contentDescription = "Image",
                    modifier = Modifier.size(180.dp, 90.dp).testTag("image"),
                )
            }
        }
        val image = onNodeWithTag("image")
        image.assertWidthIsEqualTo(180.dp).assertHeightIsEqualTo(90.dp)
        val pixels = image.captureToImage().toPixelMap()
        assertEquals(Color.Blue, pixels[1, 1])
        assertEquals(Color.Blue, pixels[pixels.width - 2, pixels.height - 2])
    }

    @Test
    fun blankUrlUsesFallbackAndRespectsContentScale() = runComposeUiTest {
        setContent {
            OnJoueTheme {
                DsImage(
                    url = "  ",
                    fallback = fallback,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).background(Color.Red).testTag("image"),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        val pixels = onNodeWithTag("image").captureToImage().toPixelMap()
        assertEquals(Color.Red, pixels[pixels.width / 2, 1])
        assertEquals(Color.Blue, pixels[pixels.width / 2, pixels.height / 2])
    }

    @Test
    fun cardImageFillsHeightAndRoundsOnlyStartCorners() = runComposeUiTest {
        setContent {
            OnJoueTheme {
                Box(Modifier.background(Color.Red)) {
                    DsCard {
                        Row(Modifier.height(IntrinsicSize.Min)) {
                            DsImage(
                                url = null,
                                fallback = fallback,
                                contentDescription = null,
                                modifier = Modifier.dsCardImage().testTag("image"),
                            )
                            Box(Modifier.size(140.dp, 100.dp))
                        }
                    }
                }
            }
        }
        val image = onNodeWithTag("image")
        image.assertWidthIsEqualTo(66.dp).assertHeightIsEqualTo(100.dp)
        val pixels = image.captureToImage().toPixelMap()
        assertEquals(Color.Red, pixels[2, 2])
        assertEquals(Color.Red, pixels[2, pixels.height - 3])
        assertEquals(Color.Blue, pixels[pixels.width - 3, 2])
        assertEquals(Color.Blue, pixels[pixels.width - 3, pixels.height - 3])
    }
}
