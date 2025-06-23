import Firebase
import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        NapierKt.initializeNapier()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
