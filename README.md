# FinanzApp

Repositorio monorepo con **3 aplicaciones independientes** del mismo dominio financiero.

Cada app vive en su carpeta, tiene su propio README y se puede ejecutar por separado.

## Estructura del repositorio

```text
FinanzApp/
├── finanz-core/   # Finanz Core (CLI Java + CSV)
├── finanz-api/    # Finanz API (Spring Boot + JPA + H2)
└── finanz-app/    # Finanz App (Android Kotlin + Compose + Room)
```

## Qué es cada app

| App | Carpeta | Objetivo | Stack |
| --- | --- | --- | --- |
| **Finanz Core** | `finanz-core/` | Versión didáctica por consola | Java puro + CSV |
| **Finanz API** | `finanz-api/` | Backend REST listo para integración | Spring Boot + JPA + H2 |
| **Finanz App** | `finanz-app/` | Cliente móvil nativo offline | Kotlin + Compose + Room |

## Quick start por módulo

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

## Cuándo usar cada una

- Usa **Finanz Core** para enseñar arquitectura en capas y fundamentos sin frameworks.
- Usa **Finanz API** para mostrar backend REST profesional y pruebas con Spring.
- Usa **Finanz App** para demostrar desarrollo Android moderno y persistencia local.

## Documentación por app

- [Finanz Core - README](finanz-core/README.md)
- [Finanz API - README](finanz-api/README.md)
- [Finanz App - README](finanz-app/README.md)
