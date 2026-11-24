# Keep rules the app's R8 run applies to :shared code.

# WebView JS bridge: the web app calls window.<BRIDGE_INTERFACE_NAME>.postMessage(...), so nothing in
# Kotlin calls these methods and R8 would otherwise remove or rename them (the bridge would go silent).
-keepattributes JavascriptInterface
-keepclassmembers class com.chukchukhaksa.mobile.common.designsystem.component.webview.** {
    @android.webkit.JavascriptInterface <methods>;
}
