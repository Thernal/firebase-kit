plugins {
    alias(libs.plugins.firebasekit.kmp.library)
    alias(libs.plugins.firebasekit.injection)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.config.api)
                implementation(projects.firebase.config.impl)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
