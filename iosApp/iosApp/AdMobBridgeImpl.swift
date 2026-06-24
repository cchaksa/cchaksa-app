import Foundation
import UIKit
import ComposeApp
import GoogleMobileAds

/// `AdMobBridge`(Kotlin, iosMain)의 Swift 구현. GoogleMobileAds의 전면 광고(`InterstitialAd`)를
/// 로드·표시하고 결과를 Kotlin 콜백(`onLoaded`/`onClosed`/`onFailure`)으로 통지한다.
///
/// `KakaoLoginBridgeImpl` 패턴 동형: `@escaping` 콜백, `KotlinThrowable` 래핑,
/// `Bundle.main` Info.plist 광고단위 ID 조회. 실제 타임아웃·결과 환원 정책은 Kotlin `IosAdManager`가 담당한다.
class AdMobBridgeImpl: AdMobBridge {

    private var interstitial: InterstitialAd?
    private var contentDelegate: InterstitialContentDelegate?

    func loadInterstitial(
        adUnitId: String?,
        onLoaded: @escaping () -> Void,
        onFailure: @escaping (KotlinThrowable) -> Void
    ) {
        // 이미 로드된 광고가 있으면 즉시 성공 통지(선택적 사전 로드 재사용).
        if interstitial != nil {
            onLoaded()
            return
        }

        let resolvedId = adUnitId
            ?? (Bundle.main.object(forInfoDictionaryKey: "ADMOB_INTERSTITIAL_AD_UNIT_ID") as? String)
            ?? ""
        if resolvedId.isEmpty {
            onFailure(KotlinThrowable(message: "not_ready"))
            return
        }

        InterstitialAd.load(with: resolvedId, request: Request()) { [weak self] ad, error in
            if error != nil {
                onFailure(KotlinThrowable(message: "network"))
                return
            }
            self?.interstitial = ad
            onLoaded()
        }
    }

    func showInterstitial(
        onClosed: @escaping () -> Void,
        onFailure: @escaping (KotlinThrowable) -> Void
    ) {
        guard let ad = interstitial, let root = Self.topViewController() else {
            onFailure(KotlinThrowable(message: "not_ready"))
            return
        }

        let delegate = InterstitialContentDelegate(
            onClosed: { [weak self] in
                self?.interstitial = nil
                self?.contentDelegate = nil
                onClosed()
            },
            onFailed: { [weak self] in
                self?.interstitial = nil
                self?.contentDelegate = nil
                onFailure(KotlinThrowable(message: "show_failed"))
            }
        )
        self.contentDelegate = delegate
        ad.fullScreenContentDelegate = delegate
        ad.present(from: root)
    }

    /// 현재 표시 중인 최상위 뷰 컨트롤러를 반환한다(전면 광고 present용).
    private static func topViewController() -> UIViewController? {
        let windowScene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
            ?? UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }.first

        guard let window = windowScene?.windows.first(where: { $0.isKeyWindow }) ?? windowScene?.windows.first,
              var top = window.rootViewController else {
            return nil
        }
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }
}

/// `FullScreenContentDelegate`를 `NSObject`로 래핑해 닫힘/표시 실패를 Swift 클로저로 전달.
private class InterstitialContentDelegate: NSObject, FullScreenContentDelegate {
    private let onClosed: () -> Void
    private let onFailed: () -> Void

    init(onClosed: @escaping () -> Void, onFailed: @escaping () -> Void) {
        self.onClosed = onClosed
        self.onFailed = onFailed
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        onClosed()
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        onFailed()
    }
}
