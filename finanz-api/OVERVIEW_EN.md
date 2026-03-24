# 💰 Finanz API

> Evolution of the [Finanz Core version](#) towards a **Complete REST API with Spring Boot**, embedded H2 database, JPA/Hibernate and integration tests with real Spring context. Same business logic, professional architecture.

---

## 🎯 What does this app do?

Exposes a REST API to manage personal financial transactions. It allows you to record income and expenses, consult them, calculate balances and obtain monthly summaries, all through HTTP endpoints with JSON responses.

**Available endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions` | Record complete transaction |
| `POST` | `/api/transactions/simple` | Record transaction with basic validation |
| `GET` | `/api/transactions` | List all transactions |
| `GET` | `/api/transactions/{id}` | Get transaction by ID |
| `DELETE` | `/api/transactions/{id}` | Delete transaction |
| `GET` | `/api/balance` | Total accumulated balance |
| `GET` | `/api/balance/current-month` | Current month balance |
| `GET` | `/api/summary/current-month` | Complete monthly summary (income, expenses, balance, transactions) |

---

## 🏗️ Architecture

```
┌───────────────────────────┐
│   BudgetController        │  ← REST layer (@RestController)
├───────────────────────────┤
│   BudgetService           │  ← Business logic (@Service)
├───────────────────────────┤
│   TransaccionRepository   │  ← Data access (JpaRepository)
├───────────────────────────┤
│   H2 Database             │  ← Embedded DB (fichero en disco)
└───────────────────────────┘
```

| Layer | Responsibility |
|------|----------------|
| `controller` | Receive HTTP requests, validate input, return `ResponseEntity` |
| `service` | Business logic: balance calculation, filtering by dates, summaries |
| `repository` | Data access via Spring Data JPA with queries derived from the method name |
| `model` | JPA entity `Transaction` mapped to table `transactions` |
| `discount` | `SimpleTransaccionRequest` for the simplified logging endpoint |

---

## 🧰 Technical stack

| Technology | Details |
|------------|----------|
| **Java** | 21 |
| **Spring Boot** | 4.0.3 |
| **Spring Data JPA** | Repository pattern with Hibernate |
| **H2 Database** | Embedded database with file persistence (`./data/finanz-api`) |
| **Maven** | Dependency management and build cycle |
| **JUnit 5** | Integration tests with `@SpringBootTest` |

---

## 🧪 Tests

The tests are **real integration tests** that bootstrap the full Spring context with an in-memory H2 database (`application-test.properties`). There are no mocks.

```java
@SpringBootTest
@ActiveProfiles("test")
class FinanzApiApplicationTests {
    // ...
}
```

**Cases covered:**
- Transaction log: verify that ID and date are assigned automatically
- Balance calculation with combined income and expenses
- Zero balance when there are no transactions
- Delete existing transaction returns `true`
- Delete non-existent ID returns `false` without throwing exception

---

## 🚀 How to run

### Option A: Try on GitHub Codespaces (without installing anything, without card)

Use the Codespaces button in the root README of the repository.

**Guide for recruiters** — 3 steps:

1. **Open**: Click the button above → sign in with GitHub if prompted
2. **Boot**: In the terminal, run `./mvnw spring-boot:run` and wait to see *"Started FinanzApiApplication"*
3. **Try**: **PORTS** tab (below) → port **8080** → **"Open in Browser"** → you will see a page with links to test the API

**No credit card required** — GitHub gives 120 hours/month free to personal accounts.

### Option B: Test the live API (if deployed)

If the API is deployed in Render/Railway/etc., you can test it directly. Replace `YOUR-URL` with the actual URL:

```bash
curl https://TU-URL/api/saldo
curl -X POST https://TU-URL/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'
```

### Option C: Run locally

**Requirements**: Java 21+, Maven 3.9+

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

### Deploy to the cloud (Render, Railway, Fly.io)

See **[DEPLOY_ES.md](DEPLOY_ES.md)** for the complete deployment guide.

### Run the tests

```bash
./mvnw test
```

### H2 Console (browse the database in the browser)

With the application running, access:

```
http://localhost:8080/h2-console
```

| Field | Worth |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/finanz-api` |
| Username | `sa` |
| Password | *(empty)* |

---

## 📬 Examples of use

### Record a transaction

```bash
curl -X POST http://localhost:8080/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'
```

### Check the balance of the month

```bash
curl http://localhost:8080/api/saldo/mes-actual
# → {"saldo": 1050.0}
```

### Get monthly summary

```bash
curl http://localhost:8080/api/resumen/mes-actual
# → {"mes": 3, "anio": 2026, "ingresos": 1800.0, "gastos": 750.0, "saldo": 1050.0, "transacciones": [...]}
```

---

## 💡 Featured Design Decisions

- **`JpaRepository` with derived query**: `findByFechaBetween` and `findByTipo` are automatically resolved by Spring Data without writing SQL, keeping the repository clean.
- **Two logging endpoints**: `/transactions` accepts the full object (ideal for integrations), `/transactions/simple` validates and builds the entity in the service (safer for direct use from the client).
- **Isolated test profile**: `application-test.properties` uses in-memory H2 with `ddl-auto=create-drop`, avoiding interference with development data.
- Explicit **`ResponseEntity`**: The handler always returns the correct HTTP code (`200`, `204`, `400`, `404`) instead of relying on default behaviors.
- **Evolution since the Finanz Core version**: The logic of `BudgetService` is identical in concept to the console version, demonstrating how a clean architecture makes it easier to migrate between interfaces.

---

## 📁 Project structure

```
src/
└── main/
    └── java/com/finanzapi/
        ├── FinanzApiApplication.java
        ├── controller/
        │   ├── BudgetController.java
        │   └── dto/
        │       └── SimpleTransaccionRequest.java
        ├── service/
        │   └── BudgetService.java
        ├── repository/
        │   └── TransaccionRepository.java
        └── model/
            ├── Transaccion.java
            └── TipoTransaccion.java
└── test/
    └── java/com/finanzapi/
        └── FinanzApiApplicationTests.java
```

---

> This project is the API version of [Finanz Core](#). Both share the same business logic and layered architecture, evolving from CSV persistence to JPA with a relational database.
