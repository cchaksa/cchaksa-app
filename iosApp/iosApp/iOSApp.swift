import Firebase
import SwiftUI
import ComposeApp
import KakaoSDKCommon
import KakaoSDKAuth
import GoogleMobileAds
import AppTrackingTransparency

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
                .onAppear {
                    requestTrackingAuthorizationIfNeeded()
                }
        }
    }

    /// ATT(App Tracking Transparency) 권한을 1회 요청한다.
    /// 프롬프트는 앱이 active 상태여야 노출되므로 약간의 지연 후 요청하고,
    /// `.notDetermined`일 때만 띄운다(재실행 시 중복 노출 방지). 허용/거부와 무관하게
    /// 광고는 정상 노출되며(거부 시 비개인화 광고로 폴백), 결과는 GoogleMobileAds가 IDFA 접근에 활용한다.
    private func requestTrackingAuthorizationIfNeeded() {
        if #available(iOS 14, *) {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                guard ATTrackingManager.trackingAuthorizationStatus == .notDetermined else { return }
                ATTrackingManager.requestTrackingAuthorization { _ in }
            }
        }
    }
}
