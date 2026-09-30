plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Matches the coordinate `QualityConventionPlugin` adds to `detektPlugins`; Gradle substitutes it
// for this project because build-logic is an included build.
group = "io.thernal.firebasekit.buildlogic"

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.junit4)
}
