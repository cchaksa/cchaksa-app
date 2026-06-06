package com.chukchukhaksa.mobile.common.designsystem.component.webview

import com.chukchukhaksa.mobile.common.kmp.isDebug

internal val webHomeUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com/mpa/home" else "https://cchaksa.com/mpa/home"

internal val sessionApiBaseUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com" else "https://cchaksa.com"

val webMyPageUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com/mpa/me" else "https://cchaksa.com/mpa/me"

val webPortalLoginUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com/portal-login" else "https://cchaksa.com/portal-login"
