# Finanz Core

> Personal budget management via CLI, written in **pure Java** with no external frameworks. Layered architecture (UI → Service → Repository), CSV persistence, and a homemade unit test runner — all from scratch.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

Part of the [FinanzApp monorepo](../README.md) — see also [`finanz-api/`](../finanz-api/) (Spring Boot REST API) and [`finanz-app/`](../finanz-app/) (Android).

## Run

```bash
javac -d out $(find . -name "*.java" ! -path "*/test/*")
java -cp out Main
```

## Run tests

```bash
javac -d out $(find . -name "*.java")
java -cp out test.TestRunner
```

## Documentation

- **Technical guide** (architecture, persistence, CLI reference): [`README_EN.md`](README_EN.md)
- **Portfolio overview**: [`OVERVIEW_EN.md`](OVERVIEW_EN.md)
