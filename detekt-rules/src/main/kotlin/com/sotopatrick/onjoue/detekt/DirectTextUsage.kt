package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

/**
 * Text is rendered with the design-system `DsText`, which maps semantic roles/tones to the theme.
 */
class DirectTextUsage(config: Config) : Rule(
    config,
    "Material/foundation Text is forbidden outside the design system module; use DsText.",
) {
    private var importedTextNames: Set<String> = emptySet()

    override fun visitKtFile(file: KtFile) {
        importedTextNames = file.importDirectives.flatMap { directive ->
            val fqName = directive.importedFqName?.asString().orEmpty()
            when {
                fqName in TEXT_FQ_NAMES -> listOf(directive.aliasName ?: fqName.substringAfterLast('.'))
                directive.isAllUnder -> TEXT_FQ_NAMES.filter { it.substringBeforeLast('.') == fqName }
                    .map { it.substringAfterLast('.') }
                else -> emptyList()
            }
        }.toSet()
        super.visitKtFile(file)
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        if (importDirective.importedFqName?.asString() in TEXT_FQ_NAMES) {
            report(Finding(Entity.from(importDirective), "Import `${importDirective.importedFqName}`. $description"))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = (expression.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        val qualified = expression.parent as? KtDotQualifiedExpression
        val isQualifiedText = qualified != null && qualified.selectorExpression == expression &&
            "${qualified.receiverExpression.text}.$name" in TEXT_FQ_NAMES
        if (isQualifiedText || (qualified == null && name in importedTextNames)) {
            report(Finding(Entity.from(expression), "Direct `$name` call. $description"))
        }
    }

    private companion object {
        val TEXT_FQ_NAMES = setOf(
            "androidx.compose.material3.Text",
            "androidx.compose.material.Text",
            "androidx.compose.foundation.text.BasicText",
        )
    }
}
