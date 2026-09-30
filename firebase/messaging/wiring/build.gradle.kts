plugins {
    alias(libs.plugins.firebasekit.kmp.library)
    alias(libs.plugins.firebasekit.injection)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.messaging.api)
                implementation(projects.firebase.messaging.impl)
            }
        }
    }
}
