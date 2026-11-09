# App-level R8 rules. Rules for :shared code live in shared/consumer-rules.pro (published as consumer keep rules).

# Crashlytics: keep line numbers so deobfuscated stack traces point at real lines,
# and keep exception class names (ApiException, PortalScrapeException, ...) readable in reports.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
