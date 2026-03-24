# 💰 Finanz API

> Evolución de la [versión Finanz Core](#) hacia una **API REST completa con Spring Boot**, base de datos H2 embebida, JPA/Hibernate y tests de integración con contexto de Spring real. Misma lógica de negocio, arquitectura profesional.

---

## 🎯 ¿Qué hace esta app?

Expone una API REST para gestionar transacciones financieras personales. Permite registrar ingresos y gastos, consultarlos, calcular saldos y obtener resúmenes mensuales, todo a través de endpoints HTTP con respuestas JSON.

**Endpoints disponibles:**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/transacciones` | Registrar transacción completa |
| `POST` | `/api/transacciones/simple` | Registrar transacción con validación básica |
| `GET` | `/api/transacciones` | Listar todas las transacciones |
| `GET` | `/api/transacciones/{id}` | Obtener transacción por ID |
| `DELETE` | `/api/transacciones/{id}` | Eliminar transacción |
| `GET` | `/api/saldo` | Saldo total acumulado |
| `GET` | `/api/saldo/mes-actual` | Saldo del mes en curso |
| `GET` | `/api/resumen/mes-actual` | Resumen mensual completo (ingresos, gastos, saldo, transacciones) |

---

## 🏗️ Arquitectura

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

| Capa | Responsabilidad |
|------|----------------|
| `controller` | Recibir requests HTTP, validar input, devolver `ResponseEntity` |
| `service` | Lógica de negocio: cálculo de saldos, filtrado por fechas, resúmenes |
| `repository` | Acceso a datos vía Spring Data JPA con queries derivadas del nombre del método |
| `model` | Entidad JPA `Transaccion` mapeada a tabla `transacciones` |
| `dto` | `SimpleTransaccionRequest` para el endpoint de registro simplificado |

---

## 🧰 Stack técnico

| Tecnología | Detalles |
|------------|----------|
| **Java** | 21 |
| **Spring Boot** | 4.0.3 |
| **Spring Data JPA** | Repository pattern con Hibernate |
| **H2 Database** | Base de datos embebida con persistencia en fichero (`./data/finanz-api`) |
| **Maven** | Gestión de dependencias y ciclo de build |
| **JUnit 5** | Tests de integración con `@SpringBootTest` |

---

## 🧪 Tests

Los tests son **tests de integración reales** que arrancan el contexto completo de Spring con una base de datos H2 en memoria (`application-test.properties`). No hay mocks.

```java
@SpringBootTest
@ActiveProfiles("test")
class FinanzApiApplicationTests {
    // ...
}
```

**Casos cubiertos:**
- Registro de transacción: verifica que se asigna ID y fecha automáticamente
- Cálculo de saldo con ingresos y gastos combinados
- Saldo cero cuando no hay transacciones
- Eliminar transacción existente devuelve `true`
- Eliminar ID inexistente devuelve `false` sin lanzar excepción

---

## 🚀 Cómo ejecutar

### Opción A: Probar en GitHub Codespaces (sin instalar nada, sin tarjeta)

Usa el botón de Codespaces del README raíz del repositorio.

**Guía para recruiters** — 3 pasos:

1. **Abrir**: Clic en el botón de arriba → inicia sesión con GitHub si te lo pide
2. **Arrancar**: En la terminal, ejecuta `./mvnw spring-boot:run` y espera a ver *"Started FinanzApiApplication"*
3. **Probar**: Pestaña **PORTS** (abajo) → puerto **8080** → **"Open in Browser"** → verás una página con enlaces para probar la API

**No requiere tarjeta de crédito** — GitHub da 120 h/mes gratis a cuentas personales.

### Opción B: Probar la API en vivo (si está desplegada)

Si la API está desplegada en Render/Railway/etc., puedes probarla directamente. Sustituye `TU-URL` por la URL real:

```bash
curl https://TU-URL/api/saldo
curl -X POST https://TU-URL/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'
```

### Opción C: Ejecutar en local

**Requisitos**: Java 21+, Maven 3.9+

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Desplegar en la nube (Render, Railway, Fly.io)

Ver **[DEPLOY_ES.md](DEPLOY_ES.md)** para la guía completa de despliegue.

### Ejecutar los tests

```bash
./mvnw test
```

### Consola H2 (explorar la BD en el navegador)

Con la aplicación en marcha, accede a:

```
http://localhost:8080/h2-console
```

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/finanz-api` |
| Username | `sa` |
| Password | *(vacío)* |

---

## 📬 Ejemplos de uso

### Registrar una transacción

```bash
curl -X POST http://localhost:8080/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'
```

### Consultar el saldo del mes

```bash
curl http://localhost:8080/api/saldo/mes-actual
# → {"saldo": 1050.0}
```

### Obtener resumen mensual

```bash
curl http://localhost:8080/api/resumen/mes-actual
# → {"mes": 3, "anio": 2026, "ingresos": 1800.0, "gastos": 750.0, "saldo": 1050.0, "transacciones": [...]}
```

---

## 💡 Decisiones de diseño destacadas

- **`JpaRepository` con query derivada**: `findByFechaBetween` y `findByTipo` se resuelven automáticamente por Spring Data sin escribir SQL, manteniendo el repositorio limpio.
- **Dos endpoints de registro**: `/transacciones` acepta el objeto completo (ideal para integraciones), `/transacciones/simple` valida y construye la entidad en el servicio (más seguro para uso directo desde cliente).
- **Perfil de test aislado**: `application-test.properties` usa H2 en memoria con `ddl-auto=create-drop`, evitando interferencias con los datos de desarrollo.
- **`ResponseEntity` explícito**: el controlador siempre devuelve el código HTTP correcto (`200`, `204`, `400`, `404`) en lugar de depender de comportamientos por defecto.
- **Evolución desde la versión Finanz Core**: la lógica de `BudgetService` es idéntica en concepto a la versión consola, lo que demuestra cómo una arquitectura limpia facilita migrar entre interfaces.

---

## 📁 Estructura del proyecto

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

> Este proyecto es la versión API de [Finanz Core](#). Ambas comparten la misma lógica de negocio y arquitectura en capas, evolucionando de persistencia CSV a JPA con base de datos relacional.
