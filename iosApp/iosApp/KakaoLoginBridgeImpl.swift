import Foundation
import ComposeApp
import KakaoSDKCommon
import KakaoSDKAuth
import KakaoSDKUser
import CryptoKit

class KakaoLoginBridgeImpl: KakaoLoginBridge {

    func isKakaoTalkAvailable() -> Bool {
        return UserApi.isKakaoTalkLoginAvailable()
    }

    func loginWithKakaoTalk(
        nonce: String,
        onSuccess: @escaping (String) -> Void,
        onFailure: @escaping (KotlinThrowable) -> Void
    ) {
        UserApi.shared.loginWithKakaoTalk(nonce: nonce) { oauthToken, error in
            if let error = error {
                let sdkError = error as NSError
                if sdkError.domain == "com.kakao.sdk", sdkError.code == -1 {
                    onFailure(KakaoTalkCancelledException())
                    return
                }
                onFailure(KotlinThrowable(message: error.localizedDescription))
                return
            }
            guard let idToken = oauthToken?.idToken else {
                onFailure(KotlinThrowable(
                    message: "id_token이 null입니다. 카카오 콘솔에서 OpenID Connect를 활성화하세요."
                ))
                return
            }
            onSuccess(idToken)
        }
    }

    func buildOAuthUrl(nonce: String) -> KakaoOAuthUrlInfo {
        let codeVerifier = generateCodeVerifier()
        let codeChallenge = generateCodeChallenge(from: codeVerifier)

        guard let appKey = Bundle.main.object(forInfoDictionaryKey: "KAKAO_NATIVE_APP_KEY") as? String else {
            fatalError("KAKAO_NATIVE_APP_KEY not found in Info.plist")
        }

        let redirectUri = "kakao\(appKey)://oauth"

        var components = URLComponents(string: "https://kauth.kakao.com/oauth/authorize")!
        components.queryItems = [
            URLQueryItem(name: "response_type", value: "code"),
            URLQueryItem(name: "client_id", value: appKey),
            URLQueryItem(name: "redirect_uri", value: redirectUri),
            URLQueryItem(name: "scope", value: "openid"),
            URLQueryItem(name: "nonce", value: nonce),
            URLQueryItem(name: "code_challenge", value: codeChallenge),
            URLQueryItem(name: "code_challenge_method", value: "S256"),
        ]

        return KakaoOAuthUrlInfo(url: components.url!.absoluteString, codeVerifier: codeVerifier)
    }

    func exchangeCodeForToken(
        code: String,
        codeVerifier: String,
        onSuccess: @escaping (String) -> Void,
        onFailure: @escaping (KotlinThrowable) -> Void
    ) {
        AuthApi.shared.token(code: code, codeVerifier: codeVerifier) { oauthToken, error in
            if let error = error {
                onFailure(KotlinThrowable(message: error.localizedDescription))
                return
            }
            guard let idToken = oauthToken?.idToken else {
                onFailure(KotlinThrowable(
                    message: "id_token이 null입니다. 카카오 콘솔에서 OpenID Connect를 활성화하세요."
                ))
                return
            }
            onSuccess(idToken)
        }
    }

    // MARK: - PKCE Helpers

    private func generateCodeVerifier() -> String {
        var bytes = [UInt8](repeating: 0, count: 32)
        _ = SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes)
        return Data(bytes)
            .base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }

    private func generateCodeChallenge(from verifier: String) -> String {
        let data = Data(verifier.utf8)
        let hash = SHA256.hash(data: data)
        return Data(hash)
            .base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }
}
