# Finanz Core (`finanz-core/`)

Personal budget application in console built with pure Java, without frameworks, with CSV persistence and layered architecture.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

---

## Index

- [Finanz Core (`finanz-core/`)](#finanz-core-finanz-core)
  - [Index](#index)
  - [1. Current status](#1-current-status)
  - [2. Features](#2-features)
  - [3. Technical architecture](#3-technical-architecture)
  - [4. Domain model](#4-domain-model)
    - [`TransactionType`](#transactiontype)
    - [`Transaction`](#transaction)
  - [5. CSV Persistence](#5-csv-persistence)
  - [6. User manual](#6-user-manual)
    - [Main menu](#main-menu)
    - [Recommended flow](#recommended-flow)
    - [Input validations](#input-validations)
  - [7. Compilation and execution](#7-compilation-and-execution)
    - [Run unit tests](#run-unit-tests)
  - [8. Technical considerations](#8-technical-considerations)
  - [9. Relationship with Finanz API and Finanz App](#9-relationship-with-finanz-api-and-finanz-app)
  - [10. Recommended roadmap](#10-recommended-roadmap)

---

## 1. Current status

Verified Status:

- The `finanz-core` code compiles correctly.
- Functional CLI flow: registration, query, summary and deletion.
- Persistence in the `transactions.csv` file is active.
- Base unit tests available for `service` and `repository`.

Testing:

```bash
javac finanz-core/**/*.java
```

---

## 2. Features

- Record income.
- Record expenses.
- List all transactions.
- Show monthly summary.
- Show balance for the current month.
- Delete transactions by ID.

---

## 3. Technical architecture

```text
src/
├── Main.java
├── model/
│   ├── TipoTransaccion.java
│   └── Transaccion.java
├── repository/
│   └── TransaccionRepository.java
├── service/
│   └── BudgetService.java
├── test/
│   ├── BudgetServiceTest.java
│   ├── TransaccionRepositoryTest.java
│   ├── TestAssertions.java
│   ├── TestUtils.java
│   └── TestRunner.java
├── util/
│   └── MoneyFormatter.java
└── ui/
    └── ConsolaMenu.java
```

Responsibilities:

- `Main`: composition of dependencies.
- `ConsolaMenu`: user interaction (CLI).
- `BudgetService`: business rules.
- `TransaccionRepository`: CSV persistence and recovery.
- `test`: unit tests without external dependencies (own runner).
- `MoneyFormatter`: common monetary format throughout the app.
- `model`: domain entities.

---

## 4. Domain model

### `TransactionType`

- `INCOME`
- `EXPENSE`

### `Transaction`

- `id: int`
- `description: String`
- `amount: double`
- `type:TransactionType`
- `date: LocalDate`

---

## 5. CSV Persistence

File used:

- `transacciones.csv` in the execution directory.

Behavior:

- With each addition and deletion, the entire file is rewritten.
- When starting, existing lines are loaded into memory.
- Incremental ID counter is maintained.

Current robustness detail:

- Badly formed lines are ignored.
- Commas are replaced in the description so as not to break the simple CSV parse.

---

## 6. User manual

### Main menu

Available options:

- `1` Register entry
- `2` Record expense
- `3` Show all transactions
- `4` Show monthly summary
- `5` Delete transaction
- `6` Show current month balance
- `0` Exit

### Recommended flow

1. Record income.
2. Record expenses.
3. Review monthly summary and balance for the current month.
4. Eliminate erroneous movements by ID.

### Input validations

- Integers and decimals are validated in a loop.
- In decimals, `,` or `.` is accepted.

---

## 7. Compilation and execution

From the root of the repository:

```bash
javac finanz-core/**/*.java && java -cp finanz-core Main
```

If your shell does not expand `**`, compile from the IDE or adjust the command according to your environment.

### Run unit tests

Compile and run:

```bash
javac finanz-core/**/*.java && java -cp finanz-core test.TestRunner
```

---

## 8. Technical considerations

- No frameworks or build managers are used in this version.
- Unit tests are executed with their own runner (`test.TestRunner`).
- CSV parsing is deliberately simple (does not support fields with commas without transformation).
- There is no editing of transactions (only registration and deletion).
- The amounts are shown in a unified monetary format (`es-ES`, EUR).

---

## 9. Relationship with Finanz API and Finanz App

- `finanz-core` is the CLI base of the domain.
- Finanz API (`finanz-api/`) exposes the same domain over HTTP.
- `finanz-app` offers simplified mobile UX for registration of movements.

Important difference:

- In `finanz-core` and `finanz-app` the entry is based on description + amount + type.

---

## 10. Recommended roadmap

- Migrate CSV to a more robust format or dedicated library.
- Add transaction editing option.
- Add filters by date/type in CLI list.
