package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

/**
 * Dimensions (`16.dp`, `14.sp`, `Dp(4f)`, `px.toDp()`, …) are design tokens owned by the design system.
 * Feature code expresses layout with the semantic enums exposed by the design system.
 */
class HardcodedDimension(config: Config) : Rule(
    config,
    "Hardcoded dp/sp/em are forbidden outside the design system module; use a design-system Spacing/Size or component.",
) {
    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val selector = (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (selector in UNIT_EXTENSIONS) {
            report(Finding(Entity.from(expression), "Hardcoded dimension `${expression.text}`. $description"))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
        if (callee.getReferencedName() in UNIT_FACTORIES) {
            report(Finding(Entity.from(expression), "Hardcoded dimension `${expression.text}`. $description"))
        }
    }

    private companion object {
        val UNIT_EXTENSIONS = setOf("dp", "sp", "em")
        val UNIT_FACTORIES = setOf("Dp", "TextUnit", "DpSize", "DpOffset", "toDp", "toSp")
    }
}
