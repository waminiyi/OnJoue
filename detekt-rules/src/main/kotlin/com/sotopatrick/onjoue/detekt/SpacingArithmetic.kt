package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression

/**
 * `Spacing` steps are public `Dp` values meant to be used as-is. Deriving new values from them
 * (`Spacing.Medium + 4.dp`, `Spacing.Large * 2`, `-Spacing.Small`) leaves the spacing scale.
 */
class SpacingArithmetic(config: Config) : Rule(
    config,
    "Arithmetic on Spacing is forbidden outside the design system module; use another Spacing step or add one.",
) {
    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.operationToken !in ARITHMETIC_TOKENS) return
        if (expression.left.isSpacingStep() || expression.right.isSpacingStep()) {
            report(Finding(Entity.from(expression), "Spacing arithmetic `${expression.text}`. $description"))
        }
    }

    override fun visitPrefixExpression(expression: KtPrefixExpression) {
        super.visitPrefixExpression(expression)
        if (expression.operationToken == KtTokens.MINUS && expression.baseExpression.isSpacingStep()) {
            report(Finding(Entity.from(expression), "Spacing arithmetic `${expression.text}`. $description"))
        }
    }

    private fun KtExpression?.isSpacingStep(): Boolean = when (this) {
        is KtParenthesizedExpression -> expression.isSpacingStep()
        is KtDotQualifiedExpression -> receiverExpression.text.substringAfterLast('.') == SPACING
        else -> false
    }

    private companion object {
        const val SPACING = "Spacing"
        val ARITHMETIC_TOKENS = setOf(
            KtTokens.PLUS,
            KtTokens.MINUS,
            KtTokens.MUL,
            KtTokens.DIV,
            KtTokens.PLUSEQ,
            KtTokens.MINUSEQ,
            KtTokens.MULTEQ,
            KtTokens.DIVEQ,
        )
    }
}
