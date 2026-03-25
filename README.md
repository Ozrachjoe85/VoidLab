# 🎵 Void Lab Music Player

**The Future of Audio. Today.**

Void Lab is an intelligent Android music player with AI-powered Auto EQ that learns from your music and automatically perfects every track you play.

## ✨ Features

- **Auto EQ Learning**: AI analyzes your music's frequency spectrum and creates custom EQ profiles for each track
- **4 Stunning Visualizers**: Spectrum, Wave, Cosmos, and Morph modes
- **10-Band Equalizer**: Full manual control with 6 professional presets
- **Glassmorphic UI**: Void aesthetic with deep blacks, electric cyans, vibrant purples, and neon greens
- **Local Playback**: Your music stays on your device—no cloud, no ads, no subscription
- **MediaSession Integration**: Lock screen controls and Android Auto support

## 🚀 Tech Stack

- **Kotlin** - Modern Android development
- **Jetpack Compose** - Declarative UI
- **Media3/ExoPlayer** - Rock-solid audio playback
- **Room Database** - Local EQ profile storage
- **Hilt** - Dependency injection
- **Android Visualizer API** - Real-time frequency analysis
- **Coroutines & Flow** - Reactive programming

## 📦 Building

This project uses GitHub Actions for automated builds:

1. Push to `main` branch
2. GitHub Actions automatically builds the APK
3. Download from Actions > Workflow Run > Artifacts

### Manual Build (requires Android Studio)
```bash
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release.apk`

## 🎨 Design Philosophy

Void Lab's interface is inspired by the cosmos—deep blacks, electric cyans, vibrant purples, hot pinks, and neon greens create a futuristic yet organic visual language.

## 📱 Requirements

- Android 8.0 (API 26) or higher
- Storage permission (access music files)
- Microphone permission (frequency analysis only—audio is NEVER recorded)

## 🔒 Privacy

- All audio processing happens locally on your device
- No internet connection required
- No analytics or tracking
- Your music library is never uploaded or shared

## 📄 License

Open source - see LICENSE file for details

---

**Made with ♥ for audiophiles**
```

---

## 🎯 **DEPLOYMENT INSTRUCTIONS**

Alright! You now have **43 complete files** for Void Lab. Here's how to deploy this to GitHub:

### **Step 1: Create the Repository**

1. Go to https://github.com/new
2. Repository name: `void-lab`
3. Description: "Intelligent Android music player with AI-powered Auto EQ"
4. Make it **Public**
5. Click "Create repository"

### **Step 2: File Structure to Create**

Use GitHub's web interface to create files in this exact structure:
```
void-lab/
├── .github/
│   └── workflows/
│       └── android.yml
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/voidlab/player/
│   │       │   ├── VoidLabApp.kt
│   │       │   ├── MainActivity.kt
│   │       │   ├── data/
│   │       │   │   ├── models/
│   │       │   │   │   ├── Song.kt
│   │       │   │   │   ├── EQProfile.kt
│   │       │   │   │   ├── Favorite.kt
│   │       │   │   │   └── AutoEQState.kt
│   │       │   │   ├── database/
│   │       │   │   │   ├── VoidLabDatabase.kt
│   │       │   │   │   ├── EQProfileDao.kt
│   │       │   │   │   └── FavoriteDao.kt
│   │       │   │   └── repository/
│   │       │   │       ├── MusicRepository.kt
│   │       │   │       ├── EQRepository.kt
│   │       │   │       └── FavoriteRepository.kt
│   │       │   ├── audio/
│   │       │   │   ├── analysis/
│   │       │   │   │   ├── FrequencyAnalyzer.kt
│   │       │   │   │   └── AutoEQLearner.kt
│   │       │   │   ├── effects/
│   │       │   │   │   └── EqualizerEngine.kt
│   │       │   │   └── playback/
│   │       │   │       └── PlaybackService.kt
│   │       │   ├── ui/
│   │       │   │   ├── theme/
│   │       │   │   │   ├── Color.kt
│   │       │   │   │   ├── Theme.kt
│   │       │   │   │   └── Type.kt
│   │       │   │   ├── screens/
│   │       │   │   │   ├── NowPlayingScreen.kt
│   │       │   │   │   ├── LibraryScreen.kt
│   │       │   │   │   ├── EqualizerScreen.kt
│   │       │   │   │   ├── VisualizerScreen.kt
│   │       │   │   │   └── SettingsScreen.kt
│   │       │   │   ├── viewmodels/
│   │       │   │   │   ├── PlayerViewModel.kt
│   │       │   │   │   ├── LibraryViewModel.kt
│   │       │   │   │   └── EQViewModel.kt
│   │       │   │   └── VoidLabNavHost.kt
│   │       │   └── di/
│   │       │       └── AppModule.kt
│   │       ├── res/
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   └── themes.xml
│   │       │   └── xml/
│   │       │       ├── backup_rules.xml
│   │       │       └── data_extraction_rules.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
└── README.md
