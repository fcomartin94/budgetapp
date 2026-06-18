# Finanz Core

Personal budget management via CLI, written in **pure Java 17** with no external frameworks. Layered architecture (UI → Service → Repository), CSV persistence, and a homemade unit test runner — all from scratch.

Part of the [FinanzApp monorepo](../README.md) — see also [`finanz-api/`](../finanz-api/) (Spring Boot REST API) and [`finanz-app/`](../finanz-app/) (Android).

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Frameworks | None — zero external dependencies |
| Persistence | CSV file (`transacciones.csv`) via `BufferedReader` / `BufferedWriter` |
| Testing | Homemade runner — no JUnit, no external libraries |
| Build | Direct `javac` compilation |

---

## Project structure

```
finanz-core/
├── Main.java
├── model/
│   ├── Transaccion.java        Domain entity
│   ├── TipoTransaccion.java    INGRESO / GASTO enum
│   └── Categoria.java
├── service/
│   └── BudgetService.java      Business logic — balances, summaries, filtering
├── repository/
│   └── TransaccionRepository.java  CSV persistence with auto-increment ID
├── ui/
│   └── ConsolaMenu.java        CLI loop — delegates everything to the service
├── util/
│   └── MoneyFormatter.java     Locale-aware EUR formatting (es-ES)
└── test/
    ├── TestRunner.java          Entry point — prints OK / FAIL per suite
    ├── TestAssertions.java      Manual assertEquals, assertTrue implementations
    ├── TestUtils.java           Temp file helpers for isolated test runs
    ├── BudgetServiceTest.java   Business logic tests
    └── TransaccionRepositoryTest.java  CSV persistence tests
```

---

## Prerequisites

- Java 17+

---

## Run

```bash
cd finanz-core
javac -d out $(find . -name "*.java" ! -path "*/test/*")
java -cp out Main
```

## Run tests

```bash
javac -d out $(find . -name "*.java")
java -cp out test.TestRunner
```

---

## CLI reference

| Option | Action |
|--------|--------|
| `1` | Record income |
| `2` | Record expense |
| `3` | List all transactions |
| `4` | Monthly summary (current month) |
| `5` | Delete transaction by ID |
| `6` | Current month balance |
| `0` | Exit |

---

## Design highlights

- **Manual dependency injection** — `BudgetService` receives `TransaccionRepository` via constructor, mirroring the pattern Spring uses internally. No static state.
- **Testable repository** — accepts a configurable CSV path so tests use temporary files without touching real data.
- **UI / logic separation** — `ConsolaMenu` never calculates anything; it delegates entirely to the service. Swapping CLI for a different interface requires no changes to the business layer.
- **CSV load tolerance** — the parser ignores extra columns to maintain backwards compatibility with older file formats.
- **Custom test runner** — `TestRunner` discovers and runs suites manually, demonstrating understanding of how testing frameworks work under the hood.

---

## License

MIT
