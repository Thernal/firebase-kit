// Plugins are declared here without applying them so every subproject's classloader shares one
// instance rather than loading its own copy.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.metro) apply false
}

// The custom Detekt rules live in the build-logic included build, so their tests are not picked up
// by the main build's `test` task on their own. Registering the aggregator on the root project is
// enough, because an unqualified `./gradlew test` runs the task in every project that has one.
val detektRulesTest = tasks.register("detektRulesTest") {
    group = "verification"
    description = "Runs the tests of the custom Detekt rule set in build-logic."
    dependsOn(gradle.includedBuild("build-logic").task(":detekt-rules:test"))
}

tasks.register("test") {
    group = "verification"
    description = "Runs the custom Detekt rule tests alongside the module tests."
    dependsOn(detektRulesTest)
}
