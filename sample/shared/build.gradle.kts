plugins {
    alias(libs.plugins.firebasekit.compose)
    alias(libs.plugins.firebasekit.injection)
}

kotlin {
    // The framework the iOS sample embeds. It exports `firebase/bridge` so Swift sees the bridge
    // protocols and PushHub — an app does the same (with build-kit: app.api.allowed).
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SampleShared"
            isStatic = true
            export(projects.firebase.bridge)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.firebase.crash.api)
                implementation(projects.firebase.crash.wiring)
                implementation(projects.firebase.config.api)
                implementation(projects.firebase.config.wiring)
                implementation(projects.firebase.messaging.api)
                implementation(projects.firebase.messaging.wiring)
                implementation(projects.firebase.auth.api)
                implementation(projects.firebase.auth.wiring)
                implementation(libs.compose.material3)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        iosMain {
            dependencies {
                api(projects.firebase.bridge)
            }
        }
    }
}
