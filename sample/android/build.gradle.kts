plugins {
    alias(libs.plugins.firebasekit.android.application)
}

dependencies {
    implementation(projects.sample.shared)
    implementation(projects.firebase.messaging.impl)
    implementation(libs.androidx.activity.compose)
}
