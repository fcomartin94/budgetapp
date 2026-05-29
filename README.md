# FinanzApp — Personal Finance Ecosystem

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

> A monorepo showcasing the same personal finance domain implemented across three different layers: a **CLI app in pure Java**, a **Spring Boot REST API**, and a **native Android app in Kotlin**. Each module is independent and runnable on its own.

---

## Modules

| Module | Stack | Purpose |
|--------|-------|---------|
| [`finanz-core/`](finanz-core/) | Pure Java, CSV | Console app — layered architecture with no frameworks |
| [`finanz-api/`](finanz-api/) | Spring Boot, JPA, H2 | REST API — professional backend with integration tests |
| [`finanz-app/`](finanz-app/) | Kotlin, Jetpack Compose, Room | Native Android app — MVVM, offline-first, reactive UI |

## Architecture progression

The three modules share the same domain model and business logic, deliberately implemented at increasing levels of abstraction:

```
finanz-core  →  manual DI, CSV persistence, custom test runner
finanz-api   →  Spring DI, JPA/Hibernate, @SpringBootTest integration tests
finanz-app   →  ViewModel + StateFlow, Room/SQLite, Jetpack Compose UI
```

This makes the repo useful for demonstrating how the same problem is solved with different tools and trade-offs.

## Quick start

### finanz-core (CLI)

```bash
cd finanz-core
javac -d out $(find . -name "*.java" ! -path "*/test/*")
java -cp out Main
```

### finanz-api (REST API)

```bash
cd finanz-api
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### finanz-app (Android)

```bash
cd finanz-app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Try finanz-api on Codespaces

1. Click **Open in GitHub Codespaces** above
2. In the terminal: `cd finanz-api && ./mvnw spring-boot:run`
3. Open the **PORTS** tab → port `8080` → **Open in Browser**

```bash
curl http://localhost:8080/api/balance
```

## Documentation

- [`finanz-core/README_EN.md`](finanz-core/README_EN.md) — CLI technical guide
- [`finanz-api/README_EN.md`](finanz-api/README_EN.md) — REST API technical guide
- [`finanz-app/README_EN.md`](finanz-app/README_EN.md) — Android technical guide
