# Add project specific ProGuard rules here.

# Keep Retrofit models
-keep class com.farmeasy.app.data.remote.** { *; }

# Keep Room entities
-keep class com.farmeasy.app.data.local.** { *; }

# Keep BLE models
-keep class com.farmeasy.app.bluetooth.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
