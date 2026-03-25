# Add project specific ProGuard rules here.
-keep class com.voidlab.player.** { *; }
-keepclassmembers class com.voidlab.player.** { *; }

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
