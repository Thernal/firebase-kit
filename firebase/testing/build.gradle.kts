plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.crash.api)
                implementation(projects.firebase.config.api)
                implementation(projects.firebase.config.impl)
                implementation(projects.firebase.messaging.api)
                implementation(projects.firebase.auth.api)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
