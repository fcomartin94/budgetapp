# Finanz App (`finanz-app/`)

Native Android application for personal financial control, developed with Kotlin, Jetpack Compose and Room.

If you're looking for a short, portfolio/demo-oriented version, check out `OVERVIEW_ES.md`.

---

## Index

- [1. Current actual state](#1-current-actual-state)
- [2. Functional vision](#2-functional-vision)
- [3. Technical stack](#3-technical-stack)
- [4. App architecture](#4-app-architecture)
- [5. Data model and persistence](#5-data-model-and-persistence)
- [6. Navigation and screens](#6-navigation-and-screens)
- [7. User manual](#7-user-manual)
- [8. Responsive behavior and UI](#8-responsive-behavior-and-ui)
- [9. Compilation and installation](#9-compile-and-installation)
- [10. Current limitations](#10-current-limitations)
- [11. Relationship with `finanz-core` and `finanz-api`](#11-relationship-with-finanz-core-and-finanz-api)
- [12. Recommended roadmap](#12-recommended-roadmap)

---

## 1. Current actual status

Current functional status:

- Operational app with local Room storage.
- Simplified form for registration of movements.
- Modernized UI (Material 3, gradients, KPI cards, income/expense differentiation).
- Inset settings for real devices (system bars and keyboard).

Persistence:

- Local database: `finanzapp_db` (Room / SQLite).

---

## 2. Functional vision

The app allows:

- Register movements (income or expense) with:
- description
- amount
- guy
- See total balance.
- View balance for the current month.
- View movement history.
- See monthly summary with:
- income
- bills
- balance
- expense/income ratio
- recent movements
- Delete movements from history.

Important:

- Category and limit are no longer captured in UI.
- Internally the default category (`General`) is saved for domain compatibility.

---

## 3. Technical stack

|Component|Worth|
| --- | --- |
|Language|Kotlin `1.9.24`|
|UI|Jetpack Compose + Material 3|
|Navigation|Navigation Compose|
|Persistence|Room `2.6.1` + SQLite|
|Architecture|MVVM + layers (`data`, `domain`, `ui`)|
|minSdk|26|
|targetSdk|35|
|compileSdk|35|
|Java/Kotlin target|17|

Main plugins:

- `com.android.application` `8.5.2`
- `org.jetbrains.kotlin.android` `1.9.24`
- `com.google.devtools.ksp` `1.9.24-1.0.20`

---

## 4. App architecture

```text
com.finanzapp/
├── MainActivity.kt
├── FinanzApp.kt
├── data/
│   ├── model/
│   │   ├── Transaccion.kt
│   │   ├── TipoTransaccion.kt
│   │   └── Categoria.kt
│   └── local/
│       ├── AppDatabase.kt
│       ├── TransaccionDao.kt
│       ├── TransaccionRepository.kt
│       └── Converters.kt
├── domain/
│   └── BudgetService.kt
└── ui/
    ├── navigation/FinanzAppNavHost.kt
    ├── viewmodel/BudgetViewModel.kt
    ├── screens/
    │   ├── HomeScreen.kt
    │   ├── TransaccionesScreen.kt
    │   └── ResumenScreen.kt
    └── theme/Theme.kt
```

Data flow:

`UI (Compose) -> ViewModel -> BudgetService -> Repository -> DAO -> Room`

Key points:

- `FinanzApp` creates DB and repository (simple manual injection).
- `BudgetViewModel` exposes `StateFlow<UiState>`.
- `Dao.getAll()` returns `Flow<List<Transaction>>` for reactive UI.

---

## 5. Data model and persistence

### Primary entity `Transaction`

- `id:Long`
- `description: String`
- `amount: Double`
- `type:TransactionType`
- `date: LocalDate`
- `category: Category` (`@Embedded(prefix = "cat_")`)

### Category

Although the UI does not ask for a category/limit, the model preserves it:

- `name: String`
- `budgetLimit: Double`

In registrations from UI:

- `name="General"`
- `budgetLimit = 0.0`

This maintains domain compatibility with other versions of the repository.

###DAO

- Complete list (Flow) ordered by descending date.
- Filter by type.
- Filter by date range.
- Insertion and deletion by ID.

---

## 6. Navigation and screens

Paths in `FinanzAppNavHost.kt`:

- `home`
- `transactions`
- `summary`

###Home

- Main card with available balance.
- KPI for balance of the month and number of movements.
- Empty state when there are no movements.
- Quick access to Transactions and Summary.

### Transactions

- Simplified form:
- description
- amount
- Income/Expense chip
- "Save movement" button.
- History with strong visual differentiation:
- `INCOME` or `EXPENSE` chip
- card color
- color sidebar
- sign in amount (`+` / `-`)
- Deletion action per item.

### Summary

- Monthly header.
- Income and expense KPI.
- Balance of the month.
- Indicator of proportion of expenditure on income.
- List of recent movements of the month.

---

## 7. User manual

### 7.1 Register a movement

1. Open `Transactions`.
2. Complete:
- description
- amount (> 0)
- type (Income/Expense)
3. Press `Save movement`.

### 7.2 Check financial health

1. In 'Home' check total balance and balance for the month.
2. In `Summary` review:
- income
- bills
- balance
- expense ratio

### 7.3 Correct data

- In `Transactions`, delete transactions with the trash can icon.

---

## 8. Responsive behavior and UI

The app uses:

- `Scaffold` with `WindowInsets.safeDrawing`.
- `navigationBarsPadding()` and `imePadding()`.
- `LazyColumn` / `verticalScroll` depending on the screen.

Aim:

- avoid clipping on real devices
- keep content and actions accessible with open keyboard

UI/Theme:

- Material 3 with dynamic scheme (Android 12+).
- Custom fallback palette.
- Global rounded `Shapes`.
- Smooth gradient backgrounds and raised cards.

---

## 9. Compilation and installation

### Requirements

- Current Android Studio.
- JDK 17 for stable build with AGP/Kotlin of this project.

### Run from Android Studio

1. Open `finanz-app` folder.
2. Sync Gradle.
3. Run `app` configuration.

### Generate debug APK

```bash
cd finanz-app
./gradlew assembleDebug
```

Expected APK:

- `app/build/outputs/apk/debug/app-debug.apk`

Installation by ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 10. Current limitations

- There is no editing of transactions (only registration and deletion).
- There are no advanced filters in history.
- There is no data export.
- There is no cloud synchronization (local offline).
- Amount format not completely homogeneous in all views (`€` and `EUR` coexist).

---

## 11. Relationship with `finanz-core` and `finanz-api`

- Shares base financial domain with both implementations.
- Android prioritizes simplified UX capture.
- Finanz API (`finanz-api/`) incorporates simplified endpoint equivalent.
- `finanz-core` retains educational CLI approach (currently with pending bugs in code).

---

## 12. Recommended roadmap

- Homogenize monetary format throughout the UI.
- Add filters by date/type in history.
- Incorporate movement editing.
- Export/import data.
- Improve form validations (inline messages).
- Prepare UI/instrumented testing.
