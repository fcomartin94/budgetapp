# Finanz API

REST API for personal budget management — the Spring Boot evolution of [Finanz Core](../finanz-core/). Same domain logic, now exposed over HTTP with **Spring Boot 3**, **Spring Data JPA**, and an embedded **H2** database.

Part of the [FinanzApp monorepo](../README.md) — see also [`finanz-core/`](../finanz-core/) (CLI) and [`finanz-app/`](../finanz-app/) (Android).

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| ORM / DB access | Spring Data JPA / Hibernate |
| Database | H2 embedded (file persistence: `./data/finanz-api`) |
| Testing | JUnit 5 + `@SpringBootTest` integration tests |
| Build | Maven 3 |

---

## Prerequisites

- Java 21
- Maven 3.9+ (or use the included `./mvnw` wrapper — no local Maven needed)

---

## Run

```bash
cd finanz-api
./mvnw spring-boot:run
# API at http://localhost:8080
```

```bash
./mvnw test
```

## Try it

```bash
# Record a transaction
curl -s -X POST http://localhost:8080/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Salary", "monto": 2000.0, "tipo": "INGRESO"}'

# Check balance
curl http://localhost:8080/api/saldo

# Monthly summary
curl http://localhost:8080/api/resumen/mes-actual
```

---

## API reference

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transacciones` | Record a full transaction |
| `POST` | `/api/transacciones/simple` | Record with minimal input (description, amount, type) |
| `GET` | `/api/transacciones` | List all transactions |
| `GET` | `/api/transacciones/{id}` | Get a transaction by ID |
| `DELETE` | `/api/transacciones/{id}` | Delete a transaction — `204 No Content` or `404` |

### Balance & summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/saldo` | All-time net balance |
| `GET` | `/api/saldo/mes-actual` | Current month balance |
| `GET` | `/api/resumen/mes-actual` | Full monthly summary — income, expenses, balance, transactions |

---

## Project structure

```
src/main/java/com/finanzapi/
├── FinanzApiApplication.java
├── controller/
│   ├── BudgetController.java          REST endpoints
│   └── dto/SimpleTransaccionRequest.java
├── service/
│   └── BudgetService.java             Business logic — balances, summaries
├── repository/
│   └── TransaccionRepository.java     Spring Data JPA with derived queries
└── model/
    ├── Transaccion.java               JPA entity (transacciones table)
    └── TipoTransaccion.java           INGRESO / GASTO enum
```

---

## H2 console

```
http://localhost:8080/h2-console
JDBC URL:  jdbc:h2:file:./data/finanz-api
Username:  sa
Password:  (empty)
```

---

## Design highlights

- **Two recording endpoints** — `/transacciones` accepts the full entity; `/transacciones/simple` validates primitives server-side for lightweight client use.
- **Derived-query repository** — `findByTipo` and `findByFechaBetween` resolved by Spring Data without manual SQL.
- **Isolated test profile** — `application-test.properties` uses `ddl-auto=create-drop` in-memory H2, no interference with dev data.
- **Architecture continuity** — `BudgetService` logic is conceptually identical to Finanz Core; the difference is delivery mechanism (CSV + CLI → JPA + REST).

---

## License

MIT
