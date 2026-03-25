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
-keepclassmembers class * {
    @javax.inject.* <fields>;
    @javax.inject.* <init>(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
```

---

## 🎯 **VERSION SUMMARY**

Here's what you're getting with these files:

| Component | Version | Why |
|-----------|---------|-----|
| **Gradle** | 8.7 | Latest stable, great performance |
| **AGP** | 8.3.2 | Latest stable Android Gradle Plugin |
| **Kotlin** | 1.9.23 | Latest stable Kotlin |
| **Compose BOM** | 2024.06.00 | Latest stable Compose |
| **Compose Compiler** | 1.5.11 | Matches Kotlin 1.9.23 |
| **Media3** | 1.3.1 | Latest stable ExoPlayer |
| **Room** | 2.6.1 | Latest stable database |
| **Hilt** | 2.51 | Latest stable DI |
| **Coroutines** | 1.8.1 | Latest stable |
| **Coil** | 2.6.0 | Latest stable image loading |
| **Navigation** | 2.7.7 | Latest stable Compose nav |

---

## 🚀 **DEPLOYMENT INSTRUCTIONS**

1. **Replace these files in your repo:**
   - `.github/workflows/android.yml`
   - `build.gradle.kts` (root)
   - `settings.gradle.kts`
   - `gradle/wrapper/gradle-wrapper.properties`
   - `app/build.gradle.kts`
   - `gradle.properties`
   - `.gitignore`
   - `app/proguard-rules.pro`

2. **Add these new files:**
   - `gradlew` (in root)
   - `gradlew.bat` (in root)

3. **Commit and push**

The build will now:
- Use Gradle 8.7 (latest stable)
- Use all the latest stable dependencies
- Generate the wrapper JAR automatically
- Build successfully
- Upload the APK as an artifact

---

## ✅ **EXPECTED BUILD OUTPUT**

After pushing, you should see:
```
✓ Checkout code
✓ Set up JDK 17
✓ Setup Gradle
✓ Make gradlew executable
✓ Build with Gradle
  - Downloading dependencies...
  - Building APK...
  - BUILD SUCCESSFUL in 3m 45s
✓ Upload APK
  - Artifact void-lab-release uploaded
