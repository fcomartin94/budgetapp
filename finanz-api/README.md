# Finanz API

> REST API for personal budget management — the Spring Boot evolution of [Finanz Core](../finanz-core/). Same domain logic, now exposed over HTTP with **Spring Boot**, **Spring Data JPA**, and an **H2** embedded database.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

Part of the [FinanzApp monorepo](../README.md) — see also [`finanz-core/`](../finanz-core/) (CLI) and [`finanz-app/`](../finanz-app/) (Android).

## Run

```bash
cd finanz-api
./mvnw spring-boot:run
# API at http://localhost:8080
```

```bash
./mvnw test
```

## Key endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions` | Record a transaction |
| `GET` | `/api/transactions` | List all transactions |
| `DELETE` | `/api/transactions/{id}` | Delete a transaction |
| `GET` | `/api/balance` | Total balance |
| `GET` | `/api/balance/current-month` | Current month balance |
| `GET` | `/api/summary/current-month` | Full monthly summary |

## Documentation

- **Technical guide** (endpoints, architecture, testing): [`README_EN.md`](README_EN.md)
- **Portfolio overview**: [`OVERVIEW_EN.md`](OVERVIEW_EN.md)
- **Deployment guide**: [`DEPLOY_EN.md`](DEPLOY_EN.md)
- **Help / FAQ**: [`HELP_EN.md`](HELP_EN.md)
