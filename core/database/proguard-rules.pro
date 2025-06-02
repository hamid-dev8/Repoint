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

# Keep all Room entity classes (annotated with @Entity)
-keepclassmembers class * {
    @androidx.room.Entity <fields>;
}

# Keep Room DAO interfaces and their methods annotated with @Query, @Insert, etc.
-keep interface androidx.room.Dao
-keep class * implements androidx.room.Dao

-keepclassmembers class * {
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}



# Keep Room's generated classes
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomOpenHelper { *; }


# Keep Parcelable implementations for entities
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}


# Keep annotations
-keepattributes *Annotation*

# Ignore missing SLF4J binding to avoid build errors
-dontwarn org.slf4j.**

# Keep Kotlin metadata (if using Kotlin)
-keep class kotlin.Metadata { *; }