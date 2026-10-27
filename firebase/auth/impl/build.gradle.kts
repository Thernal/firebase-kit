import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    alias(libs.plugins.firebasekit.kmp.library)
}

kotlin {
    // res/raw keeps default_web_client_id through an app's resource shrinking.
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        androidResources.enable = true
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.auth.api)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.auth)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.google.identity.googleid)
                implementation(libs.androidx.startup)
            }
        }
        iosMain {
            dependencies {
                implementation(projects.firebase.bridge)
            }
        }
    }
}
