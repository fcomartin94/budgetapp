# Finanz API

> The Spring Boot evolution of [Finanz Core](../finanz-core/) — same personal finance domain, now exposed as a **REST API** with Spring Data JPA, an embedded H2 database, and real integration tests. No frameworks in Core; full Spring stack here.

Part of the [FinanzApp monorepo](../README.md) · Technical guide: [`README_EN.md`](README_EN.md)

---

## What it does

Exposes a REST API for personal financial transaction management: record income and expenses, query history, calculate balances, and retrieve monthly summaries.

**Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions` | Record a full transaction |
| `POST` | `/api/transactions/simple` | Record a transaction with minimal input |
| `GET` | `/api/transactions` | List all transactions |
| `GET` | `/api/transactions/{id}` | Get transaction by ID |
| `DELETE` | `/api/transactions/{id}` | Delete a transaction |
| `GET` | `/api/balance` | Total accumulated balance |
| `GET` | `/api/balance/current-month` | Current month balance |
| `GET` | `/api/summary/current-month` | Full monthly summary (income, expenses, balance, transactions) |

---

## Architecture

```
┌───────────────────────────┐
│   BudgetController        │  ← REST layer (@RestController)
├───────────────────────────┤
│   BudgetService           │  ← Business logic (@Service)
├───────────────────────────┤
│   TransaccionRepository   │  ← Data access (JpaRepository)
├───────────────────────────┤
│   H2 Database             │  ← Embedded DB (file persistence: ./data/finanz-api)
└───────────────────────────┘
```

| Layer | Responsibility |
|-------|----------------|
| `controller` | Receive HTTP requests, validate input, return `ResponseEntity` |
| `service` | Business logic: balance calculation, date filtering, summaries |
| `repository` | Spring Data JPA with derived-query methods (no manual SQL) |
| `model` | JPA entity `Transaccion` mapped to the `transactions` table |
| `dto` | `SimpleTransaccionRequest` for the simplified recording endpoint |

---

## Technical stack

| Technology | Details |
|------------|---------|
| **Java** | 21 |
| **Spring Boot** | 3.x |
| **Spring Data JPA** | Repository pattern with Hibernate |
| **H2 Database** | Embedded with file persistence |
| **Maven** | Dependency management and build |
| **JUnit 5** | Integration tests with `@SpringBootTest` |

---

## Tests

Real integration tests — no mocks, full Spring context, in-memory H2:

```java
@SpringBootTest
@ActiveProfiles("test")
class FinanzApiApplicationTests { ... }
```

Cases covered: transaction recording, balance calculation, zero-balance state, delete (existing and non-existent IDs).

---

## How to run

### GitHub Codespaces (no local install)

Click the **Open in GitHub Codespaces** button in the root README. Then:

```bash
cd finanz-api
./mvnw spring-boot:run
```

Open **PORTS** → port `8080` → **Open in Browser**.

### Local

```bash
cd finanz-api
./mvnw spring-boot:run
# API at http://localhost:8080
```

```bash
./mvnw test
```

### H2 Console

```
http://localhost:8080/h2-console
JDBC URL:  jdbc:h2:file:./data/finanz-api
Username:  sa
Password:  (empty)
```

### Deploy to the cloud

See [`DEPLOY_EN.md`](DEPLOY_EN.md) for Render / Railway / Fly.io instructions.

---

## Design highlights

- **Derived-query repository**: `findByFechaBetween` and `findByTipo` resolved by Spring Data without manual SQL
- **Two recording endpoints**: `/transactions` for full objects; `/transactions/simple` for quick client-side use
- **Isolated test profile**: `application-test.properties` uses `ddl-auto=create-drop` in-memory H2, no interference with dev data
- **Explicit `ResponseEntity`**: controllers always return the correct HTTP status (`200`, `204`, `400`, `404`)
- **Architecture continuity**: `BudgetService` logic is conceptually identical to Finanz Core — same domain, different delivery mechanism

---

## Project structure

```
src/
├── main/java/com/finanzapi/
│   ├── FinanzApiApplication.java
│   ├── controller/
│   │   ├── BudgetController.java
│   │   └── dto/SimpleTransaccionRequest.java
│   ├── service/BudgetService.java
│   ├── repository/TransaccionRepository.java
│   └── model/
│       ├── Transaccion.java
│       └── TipoTransaccion.java
└── test/java/com/finanzapi/
    └── FinanzApiApplicationTests.java
```

> This module is the HTTP-layer evolution of [Finanz Core](../finanz-core/). Both share the same domain logic — the difference is delivery: CSV + CLI vs JPA + REST.
