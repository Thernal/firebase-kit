plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.config.api)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.config)
                implementation(libs.kotlinx.coroutines.play.services)
            }
        }
        iosMain {
            dependencies {
                implementation(projects.firebase.bridge)
            }
        }
    }
}
