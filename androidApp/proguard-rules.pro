# App-level R8 rules. Rules for :shared code live in shared/consumer-rules.pro (published as consumer keep rules).

# Crashlytics: keep line numbers so deobfuscated stack traces point at real lines,
# and keep exception class names (ApiException, PortalScrapeException, ...) readable in reports.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# --- Kakao SDK (Retrofit 2.9.0 + Gson 2.8.9) ---
# Neither version ships R8 full-mode rules, so without these R8 strips the generic signature of Call<T>
# and the app crashes on start: "Unable to create call adapter for interface retrofit2.Call
# for method com.kakao.sdk.user.UserApi...". Rules below are Retrofit's (2.10+) and Gson's (2.11+) bundled
# rules plus the Kakao SDK guide's.

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keepattributes *Annotation*
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Kakao SDK models are parsed by Gson through reflection.
-keep class com.kakao.sdk.**.model.** { <init>(...); <fields>; }
-keep interface com.kakao.sdk.**.*Api

# --- Firebase (BOM 33.2.0) ---
# Firebase instantiates the registrars listed in the manifest through their no-arg constructor. The bundled rule
# keeps only the classes, and R8 full mode then drops the constructors, so CrashlyticsRegistrar fails with
# NoSuchMethodException and Crashlytics never starts in release.
-keep class * implements com.google.firebase.components.ComponentRegistrar { <init>(); }

# --- WorkManager 2.7.1 (pulled in by Glance) ---
# WorkManager instantiates the InputMerger named in each WorkSpec through its no-arg constructor. The bundled rule
# keeps only the classes, so R8 full mode drops the constructor and every worker fails with "Could not create Input
# Merger" - including Glance's SessionWorker, which leaves the home screen widget stuck on "can't load widget".
-keep class * extends androidx.work.InputMerger { public <init>(); }

# --- Amplitude ---
# The SDK reads the advertising ID and checks Play services through reflection (Amplitude's Android guide rules).
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.google.android.gms.common.GooglePlayServicesUtil {
    int isGooglePlayServicesAvailable(android.content.Context);
}
-keep class com.google.android.gms.common.ConnectionResult { int SUCCESS; }
