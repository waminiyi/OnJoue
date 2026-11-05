import dev.detekt.gradle.extensions.DetektExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.screenshot) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
}

val installGitHooks by tasks.registering(Exec::class) {
    description = "Points git at the versioned .githooks directory (pre-commit runs detekt)."
    group = "verification"
    val isGitCheckout = rootProject.file(".git").exists()
    val isCi = providers.environmentVariable("CI").isPresent
    onlyIf { isGitCheckout && !isCi }
    commandLine("git", "config", "core.hooksPath", ".githooks")
    isIgnoreExitValue = true
}

val gitHookTriggers = setOf("preBuild", "detekt", "prepareKotlinBuildScriptModel")
tasks.matching { it.name in gitHookTriggers }.configureEach { dependsOn(installGitHooks) }

val composeRulesDetekt = libs.composeRules.detekt

subprojects {
    apply(plugin = "dev.detekt")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        parallel.set(true)
        // Analyse every KMP/Android source set (commonMain, iosMain, tests, screenshotTest…).
        source.setFrom("src")
        val moduleConfig = file("detekt.yml")
        config.setFrom(listOfNotNull(rootProject.file("config/detekt/detekt.yml"), moduleConfig.takeIf { it.exists() }))
    }

    dependencies {
        "detektPlugins"(composeRulesDetekt)
        "detektPlugins"(project(":detekt-rules"))
    }

    tasks.matching { it.name in gitHookTriggers }.configureEach { dependsOn(installGitHooks) }
}
