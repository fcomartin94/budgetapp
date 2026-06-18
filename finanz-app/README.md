# Finanz App

Native Android app for personal financial tracking — the mobile edition of the FinanzApp ecosystem. Built with **Kotlin**, **Jetpack Compose**, **Room**, and **MVVM** architecture. Fully offline, reactive UI backed by a local SQLite database.

Part of the [FinanzApp monorepo](../README.md) — see also [`finanz-core/`](../finanz-core/) (CLI) and [`finanz-api/`](../finanz-api/) (REST API).

---

## Tech stack

| Technology | Details |
|------------|---------|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Persistence | Room 2.6 (SQLite — `finanzapp_db`) |
| Architecture | MVVM — `BudgetViewModel` + `StateFlow` |
| Android SDK | minSdk 26 / targetSdk 35 / compileSdk 35 |
| Build | Gradle Kotlin DSL + KSP |

---

## Run

```bash
cd finanz-app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Or open the `finanz-app` folder in Android Studio, sync Gradle, and run the `app` configuration.

---

## Project structure

```
app/src/main/java/com/finanzapp/
├── MainActivity.kt
├── FinanzApp.kt                        Application — wires DB and repository
├── data/
│   ├── model/
│   │   ├── Transaccion.kt              Room entity (transacciones table)
│   │   ├── TipoTransaccion.kt          INGRESO / GASTO enum
│   │   └── Categoria.kt                Embedded value object
│   └── local/
│       ├── AppDatabase.kt              Room database singleton
│       ├── TransaccionDao.kt           DAO — Flow queries + suspend writes
│       ├── TransaccionRepository.kt    Repository — reactive + one-shot reads
│       └── Converters.kt              TypeConverters for LocalDate and enum
├── domain/
│   └── BudgetService.kt               Business logic — balances, summaries
└── ui/
    ├── navigation/FinanzAppNavHost.kt  Nav graph (home / transactions / summary)
    ├── viewmodel/BudgetViewModel.kt    AndroidViewModel — exposes StateFlow<UiState>
    ├── screens/
    │   ├── HomeScreen.kt               Balance overview + navigation shortcuts
    │   ├── TransaccionesScreen.kt      Registration form + transaction history
    │   └── ResumenScreen.kt            Monthly summary with KPI cards
    └── theme/Theme.kt                 Material 3 theme with dynamic color support
```

---

## Data flow

```
Room (Flow) → TransaccionRepository → BudgetService → BudgetViewModel (StateFlow) → Compose UI
```

Room emits a new list on every table change; the ViewModel collects it and recomputes all financial figures automatically — no manual refresh needed.

---

## Screens

**Home** — total balance card, current month KPI, movement count, navigation shortcuts.

**Transactions** — simplified registration form (description, amount, income/expense chip); full scrollable history with per-item delete.

**Summary** — monthly income, expenses, net balance, expense/income ratio indicator, recent movements.

---

## Design highlights

- **Offline-first** — the entire app works without a network connection; all data lives in local SQLite via Room.
- **Reactive UI** — `TransaccionDao.obtenerTodas()` returns `Flow<List<Transaccion>>`; any write is immediately reflected across all screens without explicit refresh calls.
- **MVVM with real layer separation** — the UI never calculates balances; it delegates to `BudgetViewModel` → `BudgetService`.
- **Domain continuity** — `BudgetService` shares the same financial logic as `finanz-core` and `finanz-api`; only the delivery mechanism changes.

---

## License

MIT
