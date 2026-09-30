plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.messaging.api)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
