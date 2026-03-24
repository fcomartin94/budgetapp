# 💰Finanz Core

> Personal budget management application via console, built in **pure Java** without external frameworks. Designed with a true layered architecture, CSV persistence, and proprietary unit tests written from scratch.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

---

## 🎯 What does this app do?

It allows you to record, consult and delete financial transactions (income and expenses) directly from the terminal. Data survives between sessions thanks to persistence in a CSV file.

**Main features:**
- ✅ Record income and expenses with description and automatic date
- ✅ List all transactions
- ✅ View monthly summary with broken down totals
- ✅ Check global balance and balance for the current month
- ✅ Delete transactions by ID
- ✅ Automatic persistence in CSV (load and save without user intervention)

---

## 🏗️ Architecture

The application implements a strict **layered architecture pattern**, where each layer only knows the one immediately below it:

```
┌─────────────────────┐
│     UI (Consola)    │  ← ConsolaMenu.java
├─────────────────────┤
│      Service        │  ← BudgetService.java
├─────────────────────┤
│     Repository      │  ← TransaccionRepository.java
├─────────────────────┤
│    Persistencia     │  ← transacciones.csv
└─────────────────────┘
```

| Layer | Responsibility |
|------|----------------|
| `ui` | User interaction, input reading, format validation |
| `service` | Business logic: balance calculation, filtering by month, summaries |
| `repository` | Transaction lifecycle management and CSV read/write |
| `model` | Domain entities: `Transaction`, `TransactionType`, `Category` |
| `useful` | Formatting monetary amounts with `MoneyFormatter` |

---

## 🧰 Technical stack

| Technology | Details |
|------------|----------|
| **Java** | Version 17+ |
| **No frameworks** | Zero external dependencies |
| **Persistence** | CSV file with manual reading/writing via `BufferedReader` / `BufferedWriter` |
| **Testing** | Homemade test framework (without JUnit) with own assertions |
| **Build** | Direct compilation with `javac` |

---

## 🧪 Tests

The tests are written **without JUnit or external libraries**, which demonstrates an understanding of how testing frameworks work on the inside.

```
test/
├── TestRunner.java              # Punto de entrada de los tests
├── TestAssertions.java          # Implementación manual de assertEquals, assertTrue...
├── TestUtils.java               # Helpers: rutas temporales, limpieza de ficheros
├── BudgetServiceTest.java       # Tests de lógica de negocio
└── TransaccionRepositoryTest.java  # Tests de persistencia y carga de CSV
```

**Cases covered:**
- Transaction registration and total balance calculation
- Correct filtering by month (monthly balance ignores previous months)
- Save, search by ID and delete from repository
- Load tolerant of CSV with extra columns (backwards compatibility)

---

## 🚀 How to run

### Compile

```bash
# Desde la raíz del proyecto (donde está Main.java)
javac -d out $(find . -name "*.java" ! -path "*/test/*")
```

### Run the app

```bash
java -cp out Main
```

### Run the tests

```bash
javac -d out $(find . -name "*.java")
java -cp out test.TestRunner
```

---

## 💡 Featured Design Decisions

- **Manual dependency injection**: the `BudgetService` receives the `TransaccionRepository` by constructor, it does not create it internally. This makes testing easier and reflects the same pattern that Spring uses internally.
- **Testable repository**: the repository accepts a configurable CSV path to be able to use temporary files in tests without contaminating real data.
- **UI/logical separation**: `MenuConsole` never calculates anything; delegate everything to the service. Changing the interface to GUI or API would not require touching the business logic.
- **CSV loading tolerance**: the parser ignores extra columns to maintain compatibility with previous versions of the format.

---

## 📁 Project structure

```
src/
├── Main.java
├── model/
│   ├── Transaccion.java
│   ├── TipoTransaccion.java
│   └── Categoria.java
├── service/
│   └── BudgetService.java
├── repository/
│   └── TransaccionRepository.java
├── ui/
│   └── ConsolaMenu.java
├── util/
│   └── MoneyFormatter.java
└── test/
    ├── TestRunner.java
    ├── TestAssertions.java
    ├── TestUtils.java
    ├── BudgetServiceTest.java
    └── TransaccionRepositoryTest.java
```

---

> This project is the base on which the version with REST API was built using Spring Boot. View → [budget-app-api](#)
