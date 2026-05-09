package com.chukchukhaksa.mobile.common.designsystem.component.webview

data class WebViewCookie(
  val name: String,
  val value: String,
  val domain: String,
  val path: String = "/",
  val secure: Boolean = true,
  val httpOnly: Boolean = false,
  val sameSite: String = "Lax",
  val expiresEpochSeconds: Long? = null,
)

internal fun WebViewCookie.toLogString(): String {
  val maskedValue = if (value.length <= 8) "***(${value.length})"
  else "${value.take(8)}...(${value.length})"
  return "name=$name, value=$maskedValue, domain=$domain, path=$path, " +
    "secure=$secure, httpOnly=$httpOnly, sameSite=$sameSite, expires=$expiresEpochSeconds"
}
