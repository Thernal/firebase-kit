package io.thernal.firebasekit.sample.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** What the iOS sample's SwiftUI host shows. */
fun mainViewController(): UIViewController {
    return ComposeUIViewController { SampleApp() }
}
