# Add project specific ProGuard rules here.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.receipt.scanner.api.** { *; }
-keep class com.receipt.scanner.data.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
