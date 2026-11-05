package com.sotopatrick.onjoue.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Material components are wrapped by the design system; feature code imports `Ds*` components instead.
 * Text and MaterialTheme imports are reported by their dedicated rules.
 */
class ForbiddenMaterialImport(config: Config) : Rule(
    config,
    "Material components are wrapped by the design system module; use the matching Ds* component.",
) {
    private val allowedImports: List<String> by config(emptyList())

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        val fqName = importDirective.importedFqName?.asString() ?: return
        val isMaterial = MATERIAL_PACKAGES.any { fqName == it || fqName.startsWith("$it.") }
        if (!isMaterial || fqName in HANDLED_ELSEWHERE || fqName in allowedImports) return
        report(Finding(Entity.from(importDirective), "Import `$fqName`. $description"))
    }

    private companion object {
        val MATERIAL_PACKAGES = listOf("androidx.compose.material3", "androidx.compose.material")
        val HANDLED_ELSEWHERE = setOf(
            "androidx.compose.material3.Text",
            "androidx.compose.material.Text",
            "androidx.compose.material3.MaterialTheme",
            "androidx.compose.material.MaterialTheme",
        )
    }
}
