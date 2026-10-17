# 🎮 Checkpoint

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room%20DB-003B57?style=flat-square&logo=sqlite&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)

> *A native Android platform for video game collection management and cross-platform achievement tracking.*


## ℹ️ Overview

**Checkpoint** is an Android application designed for gamers seeking to consolidate their multi-platform library, track real-time progress, and connect with friends through an integrated gamification system.

## 🌟 Key Highlights

- **Library & Progress Tracking:** Organize game collections by status (*Favorites*, *Backlog / In Progress*, *Completed*).
- **Gamification & Social Graph:** Level-up system driven by XP, unlockable badges, and friend requests via unique Friend Codes (`CKP-000000`) with granular privacy controls.
- **Reactive MVVM Architecture:** Declarative UI built with Jetpack Compose and Material Design 3, driven by `StateFlow` and Kotlin Coroutines.
- **Offline-First Strategy:** Local SQLite caching via Room for zero-latency browsing and background synchronization with Firebase (Auth & Cloud Firestore).
- **External API Aggregation:** Global catalog search and rich metadata integrated via IGDB (Twitch OAuth 2.0), Steam Web API, and RetroAchievements API.

## 🚀 Core Features

- **Library:** Horizontal carousel views sorted by progress status, fully accessible offline.
- **Explore & Search:** Query the global gaming catalog with real-time filters powered by IGDB.
- **Game Details:** Media-rich screens with synopses, artwork, community reviews, and synchronized achievement lists.
- **Profile & Gamification:** Player tier indicators, dynamic XP progression bars, showcase badges, and a shareable Friend Code.
- **Friends & Social:** Add users via code, inspect incoming requests, and browse friend profiles and library statistics.
- **Settings & Privacy:** Custom accent color theming and fine-grained data visibility (*Public*, *Friends Only*, *Private*).

## 🛠️ Tech Stack & Architecture

- **UI & Layout:** Jetpack Compose, Material Design 3, Coil (async image caching).
- **Architecture Pattern:** MVVM, Unidirectional Data Flow (UDF), Kotlin Coroutines, StateFlow.
- **Local Persistence:** Room Database (SQLite), DataStore Preferences.
- **Backend Services:** Firebase Authentication, Cloud Firestore.
- **Networking:** Retrofit, OkHttp, Moshi / Kotlinx Serialization.
- **Third-Party APIs:** IGDB API (Twitch OAuth 2.0), Steam Web API, RetroAchievements API.

## 💻 Developer Setup

To build and run the project locally:

1. Clone the repository:
   git clone https://github.com/Leon7-cmd/checkpoint.git

2. Open the project in **Android Studio** (Hedgehog or newer recommended).

3. Place your `google-services.json` file inside the `/app` directory to enable Firebase services.

4. Add your IGDB/Twitch API credentials to `local.properties`:
   TWITCH_CLIENT_ID="your_client_id_here"
   TWITCH_CLIENT_SECRET="your_client_secret_here"

5. Sync Gradle and run the app on an emulator or a physical device.

## ⬇️ Download APK

To test the pre-compiled binary on an Android device:

1. Navigate to the repository's [Releases](https://github.com/Leon7-cmd/checkpoint/releases) page.
2. Download the latest **`checkpoint-app-release.apk`**.
3. If prompted, allow installations from unknown sources in Android's security settings and install.

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
