import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            kakaoLoginBridge: KakaoLoginBridgeImpl(),
            amplitudeBridge: AmplitudeBridgeImpl(),
            adMobBridge: AdMobBridgeImpl()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(edges: .all)
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}

#if DEBUG
// 흔들기는 UIKit 응답 체인으로만 전달되므로 창에서 받아 알림으로 넘긴다.
// 디버그 빌드의 디버그 화면 진입(ShakeEffect.ios.kt)이 이 알림을 받는다.
extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        super.motionEnded(motion, with: event)
        if motion == .motionShake {
            NotificationCenter.default.post(name: Notification.Name("ChukChukHaksaDeviceDidShake"), object: nil)
        }
    }
}
#endif



