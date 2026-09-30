plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.crash.api)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.crashlytics)
            }
        }
        iosMain {
            dependencies {
                implementation(projects.firebase.bridge)
            }
        }
    }
}
