# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==================== Chaquopy ====================
-keep class com.chaquo.python.** { *; }
-keep class com.chaquo.python.PyObject { *; }

# ==================== Retrofit & GSON ====================
-keep class com.google.gson.** { *; }
-keep class com.squareup.retrofit2.** { *; }
-keep class com.squareup.okhttp3.** { *; }

# Keep all @SerializedName annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ==================== Application Classes ====================
-keep class com.example.mkvsubtitle.** { *; }
-keep class com.example.mkvsubtitle.models.** { *; }
-keep class com.example.mkvsubtitle.services.** { *; }
-keep class com.example.mkvsubtitle.utils.** { *; }

# ==================== AndroidX ====================
-keep class androidx.** { *; }
-dontwarn androidx.**

# ==================== Kotlin ====================
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ==================== Remove Logging ====================
# Remover llamadas a Log en producción
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

