# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson (or other JSON serializers) uses reflection to create objects
-keep class com.repoint.models.sharedmodels.** { *; }
-keep class com.repoint.models.sharedmodels.remote.** { *; }
-keep class com.repoint.models.sharedmodels.local.** { *; }
-keep class com.repoint.models.sharedmodels.remote.TokensBalance { *; }

# Keep fields (and their names) annotated with @SerializedName
-keepclassmembers class com.repoint.models.sharedmodels.remote.TokensBalance {
    <fields>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
# Keep annotations (needed by Gson)
-keepattributes Signature
-keepattributes *Annotation*

-keep class kotlin.Metadata { *; }
