# Finanz API (`finanz-api/`)

API REST para gestion de presupuesto personal construida con Spring Boot, Spring Data JPA e H2.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

---

## Indice

- [Finanz API (`finanz-api/`)](#finanz-api-finanz-api)
  - [Indice](#indice)
  - [1. Estado actual](#1-estado-actual)
  - [2. Ejecucion rapida](#2-ejecucion-rapida)
  - [3. Endpoints principales](#3-endpoints-principales)
  - [4. Estructura tecnica](#4-estructura-tecnica)
  - [5. Configuracion](#5-configuracion)
  - [6. Relacion con el monorepo](#6-relacion-con-el-monorepo)

---

## 1. Estado actual

Estado verificado:

- API funcional con Spring Boot + JPA + H2.
- Suite de tests verde.
- Endpoints de alta, consulta, borrado y resumen operativos.

---

## 2. Ejecucion rapida

Desde la raiz del repositorio:

```bash
cd finanz-api
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Probar rapido:

```bash
curl http://localhost:8080/api/saldo
```

Tests:

```bash
./mvnw test
```

## 3. Endpoints principales

- `POST /api/transacciones`
- `POST /api/transacciones/simple`
- `GET /api/transacciones`
- `GET /api/transacciones/{id}`
- `DELETE /api/transacciones/{id}`
- `GET /api/saldo`
- `GET /api/saldo/mes-actual`
- `GET /api/resumen/mes-actual`

## 4. Estructura tecnica

```text
finanz-api/src/main/java/com/finanzapi/
├── FinanzApiApplication.java
├── controller/
├── service/
├── repository/
└── model/
```

## 5. Configuracion

- Perfil local: `src/main/resources/application.properties`
- Perfil cloud: `src/main/resources/application-cloud.properties`
- Guia de despliegue: `DEPLOY_ES.md`

## 6. Relacion con el monorepo

- `finanz-core/` contiene la version CLI del mismo dominio.
- `finanz-app/` contiene la app Android.
- `finanz-api/` es la capa backend HTTP para el mismo modelo de negocio.
