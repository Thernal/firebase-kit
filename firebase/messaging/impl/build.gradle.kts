plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.messaging.api)
                implementation(projects.firebase.bridge)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.messaging)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.androidx.core)
                implementation(libs.lifecycle.process)
            }
        }
    }
}
