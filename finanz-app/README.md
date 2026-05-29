# Finanz App

> Native Android app for personal financial tracking — the mobile edition of the FinanzApp ecosystem. Built with **Kotlin**, **Jetpack Compose**, **Room**, and **MVVM** architecture. Fully offline, reactive UI backed by a local SQLite database.

Part of the [FinanzApp monorepo](../README.md) — see also [`finanz-core/`](../finanz-core/) (CLI) and [`finanz-api/`](../finanz-api/) (REST API).

## Run

```bash
cd finanz-app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Or open the `finanz-app` folder in Android Studio, sync Gradle, and run the `app` configuration.

## Stack

| Technology | Detail |
|------------|--------|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Persistence | Room 2.6 (SQLite) |
| Architecture | MVVM — `BudgetViewModel` + `StateFlow` |
| Android SDK | minSdk 26 / targetSdk 35 |

## Documentation

- **Technical guide** (architecture, screens, data model, build): [`README_EN.md`](README_EN.md)
- **Portfolio overview**: [`OVERVIEW_EN.md`](OVERVIEW_EN.md)
