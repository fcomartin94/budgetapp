# 📱Finanz App

> Native Android application for personal financial control, built with **Kotlin + Jetpack Compose + Room**. It maintains the same domain logic of the Finanz Core/Finanz API versions, but optimized for a modern and responsive mobile experience.

---

## 🎯 What does this app do?

It allows you to record financial movements (income and expenses), view them in real time and consult key indicators of the month, all with local offline persistence in SQLite.

**Main features:**
- ✅ Record income and expenses with a simplified form
- ✅ View total accumulated balance
- ✅ View current month balance
- ✅ Check movement history
- ✅ See monthly summary (income, expenses, balance and proportion)
- ✅ Delete movements from history
- ✅ Automatic local persistence with Room

---

## 🏗️ Architecture

```text
┌───────────────────────────┐
│   UI (Jetpack Compose)    │  ← Screens + Navigation
├───────────────────────────┤
│   ViewModel (MVVM)        │  ← BudgetViewModel (StateFlow)
├───────────────────────────┤
│   Domain Service          │  ← BudgetService
├───────────────────────────┤
│   Repository + DAO        │  ← TransaccionRepository / TransaccionDao
├───────────────────────────┤
│   Room (SQLite local)     │  ← finanzapp_db
└───────────────────────────┘
```

|Layer|Responsibility|
|------|----------------|
|`ui`|Compose rendering, user events and navigation|
|`ui.viewmodel`|Reactive state and use case coordination|
|`domain`|Financial logic: balances, summaries and business rules|
|`data.local`|Data access with Room (DAO, repository, database)|
|`data.model`|Domain entities (`Transaction`, `TransactionType`, `Category`)|

---

## 🧰 Technical stack

|Technology|Details|
|------------|----------|
|**Kotlin**|1.9.24|
|**Jetpack Compose**|Declarative UI + Material 3|
|**Navigation Compose**|Navigation between screens (`home`, `transactions`, `summary`)|
|**Room**|2.6.1 with local SQLite|
|**Architecture**|MVVM + layers (`ui`, `domain`, `data`)|
|**Android SDK**|minSdk 26 / targetSdk 35 / compileSdk 35|
|**Build**|Gradle Kotlin DSL + KSP|

---

## 📱 Main screens

###Home

- Total available balance
- KPI of monthly balance and number of movements
- Shortcuts to history and summary

### Transactions

- Registration form: description, amount and type (Income/Expense)
- History with visual differentiation by type
- Deletion of movements by item

### Summary

- Totals for the month (income, expenses, balance)
- Expense/income ratio indicator
- List of recent movements of the month

---

## 🚀 How to run

### Requirements

- Android Studio updated
- JDK 17

### Run on emulator or device

1. Open the `finanz-app` folder in Android Studio
2. Sync Gradle
3. Run `app` configuration

### Generate debug APK per terminal

```bash
cd finanz-app
./gradlew assembleDebug
```

Generated APK:

`app/build/outputs/apk/debug/app-debug.apk`

---

## 💡 Featured Design Decisions

- **Reactive UI with `Flow`/`StateFlow`**: any change in Room is automatically reflected on the screens without manual refreshes.
- **MVVM with real layer separation**: the UI does not calculate balances; delegates to `BudgetViewModel` and `BudgetService`.
- **Offline-first persistence**: the entire app works without a network by relying on local SQLite.
- **Simplified form**: category and limit are abstracted from the capture to prioritize mobile registration speed.
- **Domain compatibility**: although the UI is simple, the model shared with the other versions of the project is preserved.

---

## 📁 Project structure

```text
finanz-app/app/src/main/java/com/finanzapp/
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

---

> This app is the Android implementation of the FinanzApp ecosystem, along with the CLI and REST API versions, sharing the same functional base of personal financial management.
