plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
