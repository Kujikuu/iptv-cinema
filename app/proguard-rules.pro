# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep interface com.tviptv.app.data.xtream.XtreamApi { *; }

# Moshi
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Parcelize
-keep class com.tviptv.app.domain.model.** implements android.os.Parcelable { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Coil
-dontwarn coil.**

# Leanback
-keep class androidx.leanback.** { *; }

# TV Provider
-keep class androidx.tvprovider.** { *; }
