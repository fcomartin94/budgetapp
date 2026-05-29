# FinanzApp

Monorepo repository with **3 independent applications** from the same financial domain.

Each app lives in its own folder, has its own README and can be run separately.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

## Repository structure

```text
FinanzApp/
├── finanz-core/   # Finanz Core (CLI Java + CSV)
├── finanz-api/    # Finanz API (Spring Boot + JPA + H2)
└── finanz-app/    # Finanz App (Android Kotlin + Compose + Room)
```

## What is each app

| App | Folder | Purpose | Stack |
| --- | --- | --- | --- |
| **Finanz Core** | `finanz-core/` | Didactic console version | Pure Java + CSV |
| **Finanz API** | `finanz-api/` | REST backend ready for integration | Spring Boot + JPA + H2 |
| **Finanz App** | `finanz-app/` | Native offline mobile client | Kotlin + Compose + Room |

## Quick start per module

### Finanz Core

```bash
cd finanz-core
javac *.java model/*.java service/*.java repository/*.java ui/*.java util/*.java
java Main
```

### Finanz API

```bash
cd finanz-api
./mvnw spring-boot:run
```

### Finanz App

```bash
cd finanz-app
./gradlew assembleDebug
```

## Try Finanz API on Codespaces

From the root README button you can try `finanz-api` without installing anything:

1. Open the Codespace from the "Open in GitHub Codespaces" button.
2. In the Codespace terminal execute:

```bash
cd finanz-api
./mvnw spring-boot:run
```

1. Go to the **PORTS** tab, locate port `8080` and press **Open in Browser**.
2. Check quickly with:

```bash
curl http://localhost:8080/api/saldo
```

## When to use each one

- Use **Finanz Core** to teach layered architecture and foundations without frameworks.
- Uses **Finanz API** to show professional REST backend and testing with Spring.
- Use **Finanz App** to demonstrate modern Android development and local persistence.

## Documentation per app

| App | Overview | Technical guide |
|-----|----------|----------------|
| Finanz Core | [`finanz-core/OVERVIEW_EN.md`](finanz-core/OVERVIEW_EN.md) | [`finanz-core/README_EN.md`](finanz-core/README_EN.md) |
| Finanz API | [`finanz-api/OVERVIEW_EN.md`](finanz-api/OVERVIEW_EN.md) | [`finanz-api/README_EN.md`](finanz-api/README_EN.md) |
| Finanz App | [`finanz-app/OVERVIEW_EN.md`](finanz-app/OVERVIEW_EN.md) | [`finanz-app/README_EN.md`](finanz-app/README_EN.md) |
