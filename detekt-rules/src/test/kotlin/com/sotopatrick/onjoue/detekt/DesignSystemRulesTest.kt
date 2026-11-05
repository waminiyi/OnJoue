package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class DesignSystemRulesTest {

    @Test
    fun `raw colors are reported`() {
        val code = """
            import androidx.compose.ui.graphics.Color
            val a = Color(0xFF0B7B72)
            val b = Color.White
            val c = androidx.compose.ui.graphics.Color.Red
            val d = lightColorScheme()
        """.trimIndent()
        assertEquals(4, RawColorUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `semantic colors are allowed`() {
        val code = """
            val a = DsTone.Primary
            val b = Color.Unspecified
            val c = background.copy()
        """.trimIndent()
        assertEquals(0, RawColorUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `hardcoded dimensions are reported`() {
        val code = """
            val a = 16.dp
            val b = 14.sp
            val c = 1.5.em
            val d = Dp(4f)
            val e = size.dp
            val f = with(density) { 48.toDp() }
            val g = with(density) { px.toSp() }
        """.trimIndent()
        assertEquals(7, HardcodedDimension(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `semantic spacing is allowed`() {
        val code = """
            val a = Modifier.padding(Spacing.Medium)
            val b = DsSize.TouchTarget
        """.trimIndent()
        assertEquals(0, HardcodedDimension(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `spacing arithmetic is reported`() {
        val code = """
            val a = Spacing.Medium + 4.dp
            val b = Spacing.Large * 2
            val c = 2 * (Spacing.Small)
            val d = -Spacing.Small
            val e = Spacing.Large / 2 - other
            fun f() { var x = Dp.Unspecified; x += Spacing.Small }
        """.trimIndent()
        assertEquals(6, SpacingArithmetic(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `spacing steps used as-is are allowed`() {
        val code = """
            val a = Modifier.padding(horizontal = Spacing.Large, vertical = Spacing.Small)
            val b = Arrangement.spacedBy(Spacing.Medium)
            val c = count + 1
            val d = -offset
        """.trimIndent()
        assertEquals(0, SpacingArithmetic(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `MaterialTheme usages are reported but not imports`() {
        val code = """
            import androidx.compose.material3.MaterialTheme
            val a = MaterialTheme.colorScheme.primary
            val b = androidx.compose.material3.MaterialTheme.typography
            fun c() { MaterialTheme { } }
            val d = LocalContentColor.current
        """.trimIndent()
        assertEquals(4, DirectMaterialThemeUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `OnJoueTheme is allowed`() {
        val code = """
            fun a() { OnJoueTheme { DsText("Bonjour") } }
        """.trimIndent()
        assertEquals(0, DirectMaterialThemeUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `material Text import and calls are reported`() {
        val code = """
            import androidx.compose.material3.Text
            fun a() {
                Text("Bonjour")
                androidx.compose.foundation.text.BasicText("Salut")
            }
        """.trimIndent()
        assertEquals(3, DirectTextUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `star imported Text is reported`() {
        val code = """
            import androidx.compose.material3.*
            fun a() { Text("Bonjour") }
        """.trimIndent()
        assertEquals(1, DirectTextUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `DsText and unrelated Text are allowed`() {
        val code = """
            import com.example.Text
            fun a() {
                DsText("Bonjour")
                Text("local")
            }
        """.trimIndent()
        assertEquals(0, DirectTextUsage(Config.empty).lint(code, compile = false).size)
    }

    @Test
    fun `material imports are reported except allowed and dedicated ones`() {
        val code = """
            import androidx.compose.material3.Button
            import androidx.compose.material3.*
            import androidx.compose.material.icons.Icons
            import androidx.compose.material3.Text
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.ExperimentalMaterial3Api
            import androidx.compose.foundation.layout.Column
        """.trimIndent()
        val config = TestConfig("allowedImports" to listOf("androidx.compose.material3.ExperimentalMaterial3Api"))
        assertEquals(3, ForbiddenMaterialImport(config).lint(code, compile = false).size)
    }

    @Test
    fun `rule set exposes all rules`() {
        assertEquals(6, DesignSystemRuleSetProvider().instance().rules.size)
    }
}
