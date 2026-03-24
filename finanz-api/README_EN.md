# Finanz API (`finanz-api/`)

REST API for personal budget management built with Spring Boot, Spring Data JPA and H2.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

If you're looking for a short, portfolio/demo-oriented version, check out `OVERVIEW_ES.md`.

---

## Index

- [1. Current state](#1-current-state)
- [2. Quick execution](#2-quick-execution)
- [3. Primary endpoints](#3-main-endpoints)
- [4. Technical structure](#4-technical-structure)
- [5. Configuration](#5-configuration)
- [6. Relationship with the monorepo](#6-relationship-with-the-monorepo)

---

## 1. Current status

Verified Status:

- Functional API with Spring Boot + JPA + H2.
- Green test suite.
- Endpoints for registration, consultation, deletion and operational summary.

---

## 2. Quick execution

From the root of the repository:

```bash
cd finanz-api
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

Quick try:

```bash
curl http://localhost:8080/api/saldo
```

Tests:

```bash
./mvnw test
```

## 3. Primary endpoints

- `POST /api/transactions`
- `POST /api/transactions/simple`
- `GET /api/transactions`
- `GET /api/transactions/{id}`
- `DELETE /api/transactions/{id}`
- `GET /api/balance`
- `GET /api/balance/current-month`
- `GET /api/summary/current-month`

## 4. Technical structure

```text
finanz-api/src/main/java/com/finanzapi/
├── FinanzApiApplication.java
├── controller/
├── service/
├── repository/
└── model/
```

## 5. Configuration

- Local profile: `src/main/resources/application.properties`
- Cloud profile: `src/main/resources/application-cloud.properties`
- Deployment guide: `DEPLOY_ES.md`

## 6. Relationship with the monorepo

- `finanz-core/` contains the CLI version of the same domain.
- `finanz-app/` contains the Android app.
- `finanz-api/` is the HTTP backend layer for the same business model.
