import Firebase
import SwiftUI
import ComposeApp
import KakaoSDKCommon
import KakaoSDKAuth
import GoogleMobileAds

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        NapierKt.initializeNapier()

        guard let kakaoAppKey = Bundle.main.object(forInfoDictionaryKey: "KAKAO_NATIVE_APP_KEY") as? String else {
            fatalError("KAKAO_NATIVE_APP_KEY not found in Info.plist. Check Config.xcconfig.")
        }
        KakaoSDK.initSDK(appKey: kakaoAppKey)
        MobileAds.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    if KakaoOAuthRedirectHandler.shared.handleRedirectUrl(url: url.absoluteString) {
                        // Handled by in-app browser OAuth flow
                    } else if AuthApi.isKakaoTalkLoginUrl(url) {
                        _ = AuthController.handleOpenUrl(url: url)
                    }
                }
        }
    }
}
