package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

/**
 * Colors must come from design-system components and their semantic tones,
 * never from `Color(...)` literals or `Color.Red`-like constants.
 */
class RawColorUsage(config: Config) : Rule(
    config,
    "Raw colors are forbidden outside the design system module; use a design-system component or tone.",
) {
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
        if (callee.getReferencedName() in COLOR_FACTORIES) {
            report(Finding(Entity.from(expression), "Raw color `${expression.text}`. $description"))
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val receiver = expression.receiverExpression.text.substringAfterLast('.')
        val selector = (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (receiver == "Color" && selector in NAMED_COLORS) {
            report(Finding(Entity.from(expression), "Raw color `${expression.text}`. $description"))
        }
    }

    private companion object {
        val COLOR_FACTORIES = setOf("Color", "lightColorScheme", "darkColorScheme")
        val NAMED_COLORS = setOf(
            "Black", "DarkGray", "Gray", "LightGray", "White", "Red", "Green", "Blue",
            "Yellow", "Cyan", "Magenta", "Transparent",
        )
    }
}
