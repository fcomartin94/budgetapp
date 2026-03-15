# BudgetApp REST (`budgetapp/`)

API REST para gestion de presupuesto personal construida con Spring Boot, Spring Data JPA e H2.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/budgetapp)

**Probar sin instalar nada**: Haz clic en el boton de arriba para abrir el proyecto en GitHub Codespaces. La API se ejecuta automaticamente; en la pestana PORTS, abre el puerto 8080. No requiere tarjeta de credito.

Este documento refleja el comportamiento real del codigo actual.

Si buscas una version corta y orientada a portfolio/demo, revisa `README_PUBLIC.md`.

---

## Indice

- [1. Estado actual verificado](#1-estado-actual-verificado)
- [2. Alcance funcional](#2-alcance-funcional)
- [3. Stack y arquitectura](#3-stack-y-arquitectura)
- [4. Modelo de datos](#4-modelo-de-datos)
- [5. Reglas de negocio reales](#5-reglas-de-negocio-reales)
- [6. Endpoints](#6-endpoints)
- [7. Ejemplos de uso](#7-ejemplos-de-uso)
- [8. Configuracion y ejecucion](#8-configuracion-y-ejecucion)
- [9. Limitaciones actuales](#9-limitaciones-actuales)
- [10. Relacion con otros modulos](#10-relacion-con-otros-modulos)
- [11. Mejoras recomendadas](#11-mejoras-recomendadas)

---

## 1. Estado actual verificado

Validado localmente:

- Compilacion Maven correcta.
- Suite de tests correcta.
- Endpoints operativos para altas, consultas, borrado, saldo y resumen mensual.

Comandos ejecutados:

```bash
cd budgetapp
./mvnw -DskipTests compile
./mvnw test
```

Resultado observado:

- `BUILD SUCCESS` en compilacion.
- `Tests run: 5, Failures: 0, Errors: 0`.

---

## 2. Alcance funcional

La API permite:

- Registrar transacciones en modo completo (`POST /api/transacciones`).
- Registrar transacciones en modo simplificado (`POST /api/transacciones/simple`).
- Consultar transacciones (todas o por id).
- Eliminar transacciones por id.
- Calcular saldo total y saldo del mes actual.
- Obtener resumen mensual con detalle de transacciones.

---

## 3. Stack y arquitectura

| Componente | Valor actual |
| --- | --- |
| Lenguaje | Java |
| Framework | Spring Boot `4.0.3` |
| Persistencia | H2 |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven Wrapper |
| Version Java declarada | `21` |

Estructura principal:

```text
budgetapp/src/main/java/com/budgetapp/budgetapp/
├── BudgetappApplication.java
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
```

Flujo:

`HTTP -> Controller -> Service -> Repository -> H2`

---

## 4. Modelo de datos

### `Transaccion` (`@Entity`)

- `id: Long`
- `descripcion: String`
- `monto: double`
- `tipo: TipoTransaccion` (`INGRESO` o `GASTO`)
- `fecha: LocalDate`

---

## 5. Reglas de negocio reales

Estas reglas salen del codigo de `BudgetService` y `BudgetController`:

### Alta completa (`POST /api/transacciones`)

- `fecha` siempre se fija en `LocalDate.now()` (el valor enviado por cliente no se conserva).
- No hay validacion explicita de `monto > 0` ni de campos requeridos en este endpoint.

### Alta simplificada (`POST /api/transacciones/simple`)

- Requiere:
  - `descripcion` no nula ni en blanco.
  - `monto > 0`.
  - `tipo` no nulo.
- Si no cumple, devuelve `400 Bad Request` sin cuerpo.
- Siempre genera `fecha = LocalDate.now()`.

---

## 6. Endpoints

Base path: `/api`

| Metodo | Ruta | Descripcion | Codigos esperados |
| --- | --- | --- | --- |
| `POST` | `/transacciones` | Alta completa (payload `Transaccion`) | `200` |
| `POST` | `/transacciones/simple` | Alta simplificada (payload `SimpleTransaccionRequest`) | `200`, `400` |
| `GET` | `/transacciones` | Lista todas las transacciones | `200` |
| `GET` | `/transacciones/{id}` | Obtiene una transaccion por id | `200`, `404` |
| `DELETE` | `/transacciones/{id}` | Elimina una transaccion por id | `204`, `404` |
| `GET` | `/saldo` | Saldo total (`ingresos - gastos`) | `200` |
| `GET` | `/saldo/mes-actual` | Saldo entre primer dia del mes y hoy | `200` |
| `GET` | `/resumen/mes-actual` | Resumen mensual con lista de transacciones | `200` |

Notas:

- No hay endpoints `PUT` ni `PATCH`.
- No hay paginacion en `GET /transacciones`.
- No hay autenticacion/autorizacion.

---

## 7. Ejemplos de uso

### 7.1 Alta simplificada

Request:

```json
{
  "descripcion": "Supermercado",
  "monto": 85.5,
  "tipo": "GASTO"
}
```

Response `200` (ejemplo):

```json
{
  "id": 1,
  "descripcion": "Supermercado",
  "monto": 85.5,
  "tipo": "GASTO",
  "fecha": "2026-03-14"
}
```

### 7.2 Alta completa

Request:

```json
{
  "descripcion": "Nomina",
  "monto": 2200.0,
  "tipo": "INGRESO"
}
```

### 7.3 Consultas frecuentes

- Saldo total: `GET /api/saldo` -> `{"saldo": 1234.5}`
- Saldo mes actual: `GET /api/saldo/mes-actual` -> `{"saldo": 450.0}`
- Resumen mes actual: `GET /api/resumen/mes-actual` -> `{"mes":3,"anio":2026,"ingresos":...,"gastos":...,"saldo":...,"transacciones":[...]}`

---

## 8. Configuracion y ejecucion

### Requisitos

- JDK compatible con el proyecto (`java.version=21` en `pom.xml`).
- Maven Wrapper incluido (`./mvnw`).

### Ejecutar app

```bash
cd budgetapp
./mvnw spring-boot:run
```

### Compilar

```bash
./mvnw -DskipTests compile
```

### Ejecutar tests

```bash
./mvnw test
```

### Consola H2

- URL: `http://localhost:8080/h2-console`
- Path configurado: `/h2-console`

### Estado real de `application.properties`

Actualmente existen typos en dos claves:

- `sring.datasource.url` (deberia ser `spring.datasource.url`)
- `spring.datasource.usernmae` (deberia ser `spring.datasource.username`)

Impacto real:

- La aplicacion arranca porque Spring usa configuraciones por defecto de H2.
- La URL de datasource en archivo no se aplica como se espera mientras el typo exista.

Si se quiere persistencia en archivo como indica la URL definida, hay que corregir esas claves.

---

## 9. Limitaciones actuales

- Sin autenticacion ni autorizacion.
- Sin paginacion ni filtros en listados.
- Sin endpoint de actualizacion de transacciones.
- Validaciones hechas manualmente en controller (sin Bean Validation).
- Formato de errores no estandarizado.

---

## 10. Relacion con otros modulos

- `budgetapp` es el backend REST.
- `androidapp` consume esta API y motivó el endpoint simplificado de alta.
- `src` contiene una version anterior/alternativa del dominio y logica de negocio.

---

## 11. Mejoras recomendadas

- Corregir typos de `application.properties`.
- Migrar validaciones a Bean Validation (`@Valid`, `@NotBlank`, `@Positive`).
- Definir DTOs de respuesta y errores consistentes.
- Agregar OpenAPI/Swagger.
- Incorporar paginacion y filtros en listados.
- Expandir tests de integracion por endpoint (casos felices y de error).
