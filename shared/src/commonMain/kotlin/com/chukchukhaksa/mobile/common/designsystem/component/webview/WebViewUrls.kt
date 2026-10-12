package com.chukchukhaksa.mobile.common.designsystem.component.webview

import com.chukchukhaksa.mobile.common.kmp.isDebug

internal val webHomeUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com/mpa/home" else "https://www.cchaksa.com/mpa/home"

internal val sessionApiBaseUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com" else "https://www.cchaksa.com"

val webMyPageUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com/mpa/me" else "https://www.cchaksa.com/mpa/me"

val webPortalLoginUrl: String
  get() = if (isDebug) "https://dv.cchaksa.com/mpa/portal-login" else "https://www.cchaksa.com/mpa/portal-login"
