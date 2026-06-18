# FinanzApp — Personal Finance Ecosystem

> A monorepo showcasing the same personal finance domain implemented across three independent layers: a **CLI app in pure Java**, a **Spring Boot REST API**, and a **native Android app in Kotlin**. Each module is self-contained and runnable on its own.

---

## Modules

| Module | Stack | Purpose |
|--------|-------|---------|
| [`finanz-core/`](finanz-core/) | Pure Java 17, CSV | Console app — layered architecture, no frameworks, custom test runner |
| [`finanz-api/`](finanz-api/) | Spring Boot 3, JPA, H2 | REST API — professional backend with real integration tests |
| [`finanz-app/`](finanz-app/) | Kotlin, Jetpack Compose, Room | Native Android app — MVVM, offline-first, reactive UI |

---

## Architecture progression

The three modules share the same domain model and business logic, deliberately implemented at increasing levels of abstraction:

```
finanz-core  →  manual DI, CSV persistence, custom test runner
finanz-api   →  Spring DI, JPA/Hibernate, @SpringBootTest integration tests
finanz-app   →  ViewModel + StateFlow, Room/SQLite, Jetpack Compose UI
```

This makes the repo useful for demonstrating how the same problem is solved with different tools and trade-offs.

---

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
# API at http://localhost:8080
```

### finanz-app (Android)

```bash
cd finanz-app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## License

MIT
