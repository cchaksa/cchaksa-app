package com.chukchukhaksa.mobile.common.kmp

import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSURLComponents

object KakaoOAuthRedirectHandler {

    private var pendingDeferred: CompletableDeferred<String>? = null

    fun prepare(): CompletableDeferred<String> {
        pendingDeferred?.cancel()
        val deferred = CompletableDeferred<String>()
        pendingDeferred = deferred
        return deferred
    }

    fun handleRedirectUrl(url: String): Boolean {
        val deferred = pendingDeferred ?: return false

        val components = NSURLComponents(string = url) ?: run {
            deferred.completeExceptionally(IllegalStateException("Invalid redirect URL: $url"))
            pendingDeferred = null
            return true
        }

        val queryItems = components.queryItems.orEmpty()

        val error = queryItems
            .filterIsInstance<platform.Foundation.NSURLQueryItem>()
            .firstOrNull { it.name == "error" }
            ?.value

        if (error != null) {
            val description = queryItems
                .filterIsInstance<platform.Foundation.NSURLQueryItem>()
                .firstOrNull { it.name == "error_description" }
                ?.value ?: error
            deferred.completeExceptionally(IllegalStateException("OAuth error: $description"))
            pendingDeferred = null
            return true
        }

        val code = queryItems
            .filterIsInstance<platform.Foundation.NSURLQueryItem>()
            .firstOrNull { it.name == "code" }
            ?.value

        if (code != null) {
            deferred.complete(code)
        } else {
            deferred.completeExceptionally(IllegalStateException("No authorization code in redirect URL"))
        }

        pendingDeferred = null
        return true
    }

    fun cancel() {
        pendingDeferred?.cancel()
        pendingDeferred = null
    }
}
