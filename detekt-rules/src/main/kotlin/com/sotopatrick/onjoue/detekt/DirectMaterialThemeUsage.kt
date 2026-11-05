package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * Theme tokens (`MaterialTheme.colorScheme`, `.typography`, `.shapes`) stay private to the design system.
 */
class DirectMaterialThemeUsage(config: Config) : Rule(
    config,
    "MaterialTheme is private to the design system module; use OnJoueTheme and design-system components.",
) {
    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        val name = expression as? KtNameReferenceExpression ?: return
        if (name.getReferencedName() !in THEME_NAMES) return
        if (name.getStrictParentOfType<KtImportDirective>() != null) return
        report(Finding(Entity.from(name), "Direct `${name.getReferencedName()}` usage. $description"))
    }

    private companion object {
        val THEME_NAMES = setOf("MaterialTheme", "LocalContentColor", "LocalTextStyle")
    }
}
