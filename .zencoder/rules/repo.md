---
description: Repository Information Overview
alwaysApply: true
---

# MKV Subtitle Extractor Information

## Summary

MKV Subtitle Extractor is a native Android application that enables users to extract, visualize, and translate subtitles from MKV (Matroska) video files. The application integrates FFmpeg for subtitle extraction, LibreTranslate for multilingual translation, and Chaquopy for Python script execution. It supports 24+ languages and handles Android 5.0+ (API 21-34) with proper runtime permissions management.

## Structure

```
MKVSubtitleExtractor/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mkvsubtitle/
│   │   │   │   ├── MainActivity.kt          (File selection & extraction UI)
│   │   │   │   ├── SubtitleDisplayActivity.kt (Subtitle display & translation)
│   │   │   │   ├── services/
│   │   │   │   │   ├── SubtitleExtractionService.kt
│   │   │   │   │   └── TranslationService.kt
│   │   │   │   ├── models/
│   │   │   │   │   └── Models.kt
│   │   │   │   └── utils/
│   │   │   │       └── FileUtils.kt
│   │   │   ├── python/
│   │   │   │   ├── extract_subtitles.py    (FFmpeg-based extraction)
│   │   │   │   └── translate_subtitles.py  (LibreTranslate API client)
│   │   │   ├── res/
│   │   │   │   ├── layout/                 (XML UI layouts)
│   │   │   │   ├── values/                 (Colors, strings, themes)
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── build/                          (Build outputs)
│   └── build.gradle
├── gradle/
│   └── wrapper/
├── build.gradle                             (Root-level Gradle config)
├── settings.gradle
├── gradle.properties
├── gradlew & gradlew.bat                   (Gradle wrapper executables)
└── Documentation files (README, BUILD_INSTRUCTIONS, etc.)
```

## Language & Runtime

**Language**: Kotlin (Android) + Python  
**Kotlin Version**: 1.9.22  
**Java/JVM Target**: Java 17  
**Android API**: Minimum API 21 (Android 5.0), Target API 34 (Android 14)  
**Build System**: Gradle 8.3.0+  
**Package Manager**: Gradle (Android), pip (Python via Chaquopy)

## Dependencies

**Main Dependencies**:
- `androidx.core:core-ktx:1.12.0`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.11.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `androidx.activity:activity-ktx:1.8.0`
- `androidx.fragment:fragment-ktx:1.6.1`
- `com.squareup.retrofit2:retrofit:2.9.0`
- `com.squareup.okhttp3:okhttp:4.11.0`
- `com.google.code.gson:gson:2.10.1`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`

**Development Dependencies**:
- `junit:junit:4.13.2`
- `androidx.test.ext:junit:1.1.5`
- `androidx.test.espresso:espresso-core:3.5.1`

**Python Dependencies** (via Chaquopy):
- `ffmpeg-python`
- `requests`
- `pymkv`
- `pysrt`

## Build & Installation

```bash
# Clean build
./gradlew clean build

# Build with stack trace for debugging
./gradlew clean build --stacktrace

# Install debug APK to connected device/emulator
./gradlew installDebug

# Run application
./gradlew run

# Build release APK
./gradlew assembleRelease

# Create bundle for Play Store
./gradlew bundleRelease

# Execute tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Static code analysis
./gradlew lint
```

## Main Files & Resources

**Application Entry Points**:
- `MainActivity.kt`: Main activity handling file selection, permissions, and UI initialization
- `SubtitleDisplayActivity.kt`: Activity for displaying extracted subtitles and managing translation

**Services**:
- `SubtitleExtractionService.kt`: Orchestrates FFmpeg extraction via Python bridge
- `TranslationService.kt`: Manages LibreTranslate API communication with retry logic and rate limit handling

**Python Scripts**:
- `extract_subtitles.py`: FFmpeg wrapper for MKV subtitle extraction, supports SRT/ASS/SSA formats
- `translate_subtitles.py`: LibreTranslate client with multi-language support (24+ languages)

**UI Resources**:
- `activity_main.xml`: File picker interface layout
- `activity_subtitle_display.xml`: Subtitle viewer and translator interface layout
- `strings.xml`: Localized text strings
- `colors.xml`: Material Design color palette
- `themes.xml`: Material Design theme definitions

## Configuration

**Build Configuration** (`build.gradle`):
- `compileSdk`: 34
- `minSdk`: 21
- `targetSdk`: 34
- `versionCode`: 1
- `versionName`: 1.0.0
- `ndk.abiFilters`: arm64-v8a, armeabi-v7a, x86, x86_64

**Gradle Properties**:
- JVM args: `-Xmx2048m -XX:MaxPermSize=512m`
- AndroidX enabled
- Jetifier enabled
- Parallel builds enabled
- Gradle daemon enabled

## Android Permissions

```xml
<!-- File access -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- Network access -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**Runtime Permissions**: The application requests `READ_EXTERNAL_STORAGE` at runtime (Android 6+) and handles Android 11+ Scoped Storage requirements.

## Key Features & Workflow

1. **Subtitle Extraction**: FFmpeg extracts subtitles from MKV containers via Python subprocess
2. **Format Support**: SRT, ASS, SSA, VOB, DVD subtitle formats
3. **Translation**: LibreTranslate API provides multilingual translation (ES, FR, DE, ZH, AR, JA, PT, RU, KO, IT, NL, PL, TR, EL, HU, SV, FI, NO, DA, CS, RO, VI, TH, ID)
4. **Storage**: Subtitle files cached in application directory
5. **Coroutines**: Asynchronous operations prevent UI blocking

## API Integration

**LibreTranslate**: `https://libretranslate.com/translate`
- Supports auto-language detection
- Rate limit handling with exponential backoff (3 retry attempts)
- 30-second connection/read/write timeouts

## Known Limitations

- Requires FFmpeg binaries on device
- LibreTranslate rate limiting for high-volume translations
- Large MKV files (>500MB) may cause performance degradation
- Requires internet connection for translation functionality
- Android 11+ uses Scoped Storage with limited file access
