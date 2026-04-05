# Keep Retrofit interfaces
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# Keep data models for Gson
-keep class com.cloudmonitor.app.data.model.** { *; }
# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
