plugins {
    alias(libs.plugins.firebasekit.kmp.library)
    alias(libs.plugins.firebasekit.injection)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.crash.api)
                implementation(projects.firebase.crash.impl)
            }
        }
    }
}
