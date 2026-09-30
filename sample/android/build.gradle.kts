plugins {
    alias(libs.plugins.firebasekit.android.application)
}

// With a google-services.json beside this file (config/firebase explains it), the sample runs against
// that Firebase project; without one — as committed — Firebase stays unconfigured and says so.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

dependencies {
    implementation(projects.sample.shared)
    implementation(projects.firebase.messaging.impl)
    implementation(libs.androidx.activity.compose)
}
