package com.chukchukhaksa.mobile.common.kmp

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class AppleSignInClient actual constructor() {

    // Strong reference 유지 (delegate/controller가 ARC로 해제되지 않도록)
    private var currentDelegate: AppleSignInDelegate? = null
    private var currentController: ASAuthorizationController? = null

    actual suspend fun signIn(): AppleSignInResult = suspendCancellableCoroutine { continuation ->
        val rawNonce = NSUUID().UUIDString()
        val hashedNonce = sha256Hex(rawNonce)

        val delegate = AppleSignInDelegate(continuation, rawNonce) {
            currentDelegate = null
            currentController = null
        }
        currentDelegate = delegate

        val provider = ASAuthorizationAppleIDProvider()
        val request = provider.createRequest()
        request.requestedScopes = listOf(
            ASAuthorizationScopeEmail,
            ASAuthorizationScopeFullName,
        )
        request.nonce = hashedNonce

        val controller = ASAuthorizationController(
            authorizationRequests = listOf(request),
        )
        controller.delegate = delegate
        controller.presentationContextProvider = delegate
        currentController = controller

        continuation.invokeOnCancellation {
            currentDelegate = null
            currentController = null
        }

        controller.performRequests()
    }
}

// ASAuthorizationError.canceled (사용자가 애플 로그인 시트를 취소)
private const val APPLE_AUTH_CANCELED_CODE = 1001L

private class AppleSignInDelegate(
    private val continuation: CancellableContinuation<AppleSignInResult>,
    private val rawNonce: String,
    private val onComplete: () -> Unit,
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    @OptIn(BetaInteropApi::class)
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
            ?: run {
                onComplete()
                continuation.resumeWithException(
                    IllegalStateException("Invalid credential type"),
                )
                return
            }

        val identityTokenData = credential.identityToken
        val authorizationCodeData = credential.authorizationCode

        if (identityTokenData == null || authorizationCodeData == null) {
            onComplete()
            continuation.resumeWithException(
                IllegalStateException("Missing identityToken or authorizationCode"),
            )
            return
        }

        val identityToken = NSString.create(
            data = identityTokenData,
            encoding = NSUTF8StringEncoding,
        )?.toString() ?: run {
            onComplete()
            continuation.resumeWithException(
                IllegalStateException("Failed to decode identityToken"),
            )
            return
        }

        val authorizationCode = NSString.create(
            data = authorizationCodeData,
            encoding = NSUTF8StringEncoding,
        )?.toString() ?: run {
            onComplete()
            continuation.resumeWithException(
                IllegalStateException("Failed to decode authorizationCode"),
            )
            return
        }

        val fullName = credential.fullName?.let { nameComponents ->
            listOfNotNull(
                nameComponents.familyName,
                nameComponents.givenName,
            ).joinToString(" ").ifBlank { null }
        }

        onComplete()
        continuation.resume(
            AppleSignInResult(
                identityToken = identityToken,
                authorizationCode = authorizationCode,
                userId = credential.user,
                email = credential.email,
                fullName = fullName,
                nonce = rawNonce,
            ),
        )
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        onComplete()
        // 사용자가 애플 로그인 시트를 직접 취소(ASAuthorizationError.canceled = 1001)한 경우는
        // 실패가 아닌 취소로 전달해, 화면에서 오류 토스트가 뜨지 않도록 한다.
        val error = if (didCompleteWithError.code == APPLE_AUTH_CANCELED_CODE) {
            LoginCancelledException()
        } else {
            Exception("Apple Sign In failed: ${didCompleteWithError.localizedDescription}")
        }
        continuation.resumeWithException(error)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController,
    ): ASPresentationAnchor {
        val windowScene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()

        return windowScene?.windows
            ?.filterIsInstance<UIWindow>()
            ?.firstOrNull { it.isKeyWindow() }
            ?: UIWindow()
    }
}
