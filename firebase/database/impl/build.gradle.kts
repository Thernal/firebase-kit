plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.database.api)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.database)
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
