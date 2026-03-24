# Cómo se hizo: Finanz API — API REST con Spring Boot

> Documento técnico de construcción paso a paso. El objetivo es que cualquier persona con conocimientos básicos de Java y Spring Boot pueda replicar esta aplicación desde cero y desplegarla en la nube.

---

## ¿Qué hace esta aplicación?

Es la evolución de la app Finanz Core. Expone la misma lógica de gestión de presupuesto personal a través de una **API REST** construida con Spring Boot. Los datos se guardan en una base de datos H2 embebida (en disco en desarrollo, en memoria en cloud). Incluye un pequeño frontend estático con HTML puro para explorar los endpoints desde el navegador, y está preparada para desplegarse en Render, Railway o Fly.io con Docker.

---

## Requisitos previos

- Java 21 (la versión que usa el proyecto)
- Maven (o usar el wrapper `./mvnw` incluido, que no requiere instalación)
- Docker (solo si se quiere construir la imagen)

---

## Cómo arrancar el proyecto

```bash
# Clonar o descomprimir el proyecto
cd finanz-api # carpeta del módulo Finanz API

# Arrancar en local
./mvnw spring-boot:run

# Visitar en el navegador
http://localhost:8080
```

Spring Boot arranca en el puerto 8080 por defecto. La página de inicio en `http://localhost:8080` muestra los endpoints disponibles con enlaces clicables.

---

## Estructura del proyecto

```
finanz-api/
├── src/
│   ├── main/
│   │   ├── java/com/finanzapi/
│   │   │   ├── FinanzApiApplication.java         ← punto de entrada
│   │   │   ├── controller/
│   │   │   │   ├── BudgetController.java          ← endpoints REST
│   │   │   │   └── dto/
│   │   │   │       └── SimpleTransaccionRequest.java
│   │   │   ├── model/
│   │   │   │   ├── TipoTransaccion.java
│   │   │   │   └── Transaccion.java               ← entidad JPA
│   │   │   ├── repository/
│   │   │   │   └── TransaccionRepository.java     ← interfaz JPA
│   │   │   └── service/
│   │   │       └── BudgetService.java
│   │   └── resources/
│   │       ├── application.properties             ← config local (H2 en disco)
│   │       ├── application-cloud.properties       ← config cloud (H2 en memoria)
│   │       └── static/
│   │           └── index.html                     ← frontend estático
│   └── test/
│       └── java/com/finanzapi/
│           └── FinanzApiApplicationTests.java
├── Dockerfile
├── pom.xml
├── render.yaml
├── railway.json
└── fly.toml
```

---

## Paso 1 — Crear el proyecto base con Spring Initializr

Se accede a [start.spring.io](https://start.spring.io) y se configura:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 4.0.x
- **Java**: 21
- **Dependencies**:
  - Spring Web (para la API REST)
  - Spring Data JPA (para la capa de persistencia)
  - H2 Database (base de datos embebida)

Se descarga el ZIP generado, se descomprime y se abre en el IDE. Esto ya proporciona el `pom.xml`, el wrapper de Maven y la clase principal.

**`pom.xml`** — las dependencias clave quedan así:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## Paso 2 — Configurar la base de datos H2

Se edita `src/main/resources/application.properties`:

```properties
spring.application.name=finanz-api

# H2 persistida en disco (no se pierde al reiniciar)
spring.datasource.url=jdbc:h2:file:./data/finanz-api
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Hibernate crea o actualiza tablas automáticamente
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Consola web para inspeccionar la BD durante el desarrollo
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

`jdbc:h2:file:./data/finanz-api` indica a H2 que guarde los datos en un archivo dentro de la carpeta `data/` del proyecto. Con `ddl-auto=update`, Hibernate crea las tablas la primera vez y las actualiza si cambia el modelo, sin borrar datos existentes.

La consola H2 en `/h2-console` permite conectarse al navegador y ejecutar SQL directamente, muy útil durante el desarrollo.

---

## Paso 3 — El modelo de datos con JPA

A diferencia de la versión Finanz Core, aquí `Transaccion` es una **entidad JPA**: lleva anotaciones que le dicen a Hibernate cómo mapearla a una tabla SQL.

**`model/TipoTransaccion.java`**

```java
package com.finanzapi.model;

public enum TipoTransaccion {
    INGRESO,
    GASTO
}
```

**`model/Transaccion.java`**

```java
package com.finanzapi.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;         // Long en lugar de int: JPA usa Long para IDs

    private String descripcion;
    private double monto;

    @Enumerated(EnumType.STRING)   // guarda "INGRESO"/"GASTO" en texto, no como 0/1
    private TipoTransaccion tipo;

    private LocalDate fecha;

    public Transaccion() {}    // constructor vacío obligatorio para JPA

    public Transaccion(String descripcion, double monto, TipoTransaccion tipo, LocalDate fecha) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    // getters y setters...
}
```

Puntos clave:
- `@Entity` y `@Table(name = "transacciones")` mapean la clase a la tabla SQL.
- `@Id` + `@GeneratedValue(IDENTITY)` delegan la gestión del ID a la base de datos (autoincrement).
- `@Enumerated(EnumType.STRING)` hace que el enum se persista como texto, no como número. Es más legible y no se rompe si se reordena el enum.
- El **constructor vacío** sin parámetros es obligatorio para JPA/Hibernate.

---

## Paso 4 — El repositorio JPA

En Spring Data JPA, el repositorio se declara como una **interfaz**. Spring genera la implementación en tiempo de ejecución.

**`repository/TransaccionRepository.java`**

```java
package com.finanzapi.repository;

import com.finanzapi.model.TipoTransaccion;
import com.finanzapi.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByTipo(TipoTransaccion tipo);

    List<Transaccion> findByFechaBetween(LocalDate inicio, LocalDate fin);
}
```

`JpaRepository<Transaccion, Long>` ya proporciona `save()`, `findAll()`, `findById()`, `deleteById()`, `existsById()` y más, sin escribir una sola línea de SQL.

Los dos métodos adicionales (`findByTipo` y `findByFechaBetween`) se generan automáticamente a partir del nombre: Spring interpreta la convención `findBy{Campo}` y construye la consulta SQL correspondiente.

---

## Paso 5 — La capa de servicio

El servicio es un bean Spring anotado con `@Service`. Spring Boot lo detecta automáticamente y gestiona su ciclo de vida. La inyección del repositorio se hace por constructor (la forma recomendada).

**`service/BudgetService.java`**

```java
package com.finanzapi.service;

import com.finanzapi.model.*;
import com.finanzapi.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class BudgetService {

    private final TransaccionRepository repositorio;

    public BudgetService(TransaccionRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Transaccion registrar(Transaccion transaccion) {
        transaccion.setFecha(LocalDate.now());
        return repositorio.save(transaccion);
    }

    public Transaccion registrarSimple(String descripcion, double monto, TipoTransaccion tipo) {
        return repositorio.save(new Transaccion(descripcion, monto, tipo, LocalDate.now()));
    }

    public List<Transaccion> obtenerTodas() {
        return repositorio.findAll();
    }

    public Optional<Transaccion> obtenerPorId(Long id) {
        return repositorio.findById(id);
    }

    public boolean eliminar(Long id) {
        if (repositorio.existsById(id)) {
            repositorio.deleteById(id);
            return true;
        }
        return false;
    }

    public double calcularSaldo() {
        double ingresos = repositorio.findByTipo(TipoTransaccion.INGRESO)
                .stream().mapToDouble(Transaccion::getMonto).sum();
        double gastos = repositorio.findByTipo(TipoTransaccion.GASTO)
                .stream().mapToDouble(Transaccion::getMonto).sum();
        return ingresos - gastos;
    }

    public double calcularSaldoMesActual() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();
        List<Transaccion> transacciones = repositorio.findByFechaBetween(inicio, fin);

        double ingresos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto).sum();
        double gastos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto).sum();

        return ingresos - gastos;
    }

    public Map<String, Object> obtenerResumenMensual() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();
        List<Transaccion> transacciones = repositorio.findByFechaBetween(inicio, fin);

        double ingresos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto).sum();
        double gastos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto).sum();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("mes", LocalDate.now().getMonthValue());
        resumen.put("anio", LocalDate.now().getYear());
        resumen.put("ingresos", ingresos);
        resumen.put("gastos", gastos);
        resumen.put("saldo", ingresos - gastos);
        resumen.put("transacciones", transacciones);
        return resumen;
    }
}
```

`calcularSaldoMesActual()` usa `withDayOfMonth(1)` para obtener el primer día del mes actual. Así la consulta `findByFechaBetween(inicio, fin)` acota exactamente el mes en curso.

---

## Paso 6 — El controlador REST

El controlador expone los endpoints HTTP. Está anotado con `@RestController` (equivalente a `@Controller` + `@ResponseBody`): cada método devuelve automáticamente JSON.

**`controller/BudgetController.java`**

```java
package com.finanzapi.controller;

import com.finanzapi.controller.dto.SimpleTransaccionRequest;
import com.finanzapi.model.Transaccion;
import com.finanzapi.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService servicio;

    public BudgetController(BudgetService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/transacciones")
    public ResponseEntity<Transaccion> registrar(@RequestBody Transaccion transaccion) {
        return ResponseEntity.ok(servicio.registrar(transaccion));
    }

    @PostMapping("/transacciones/simple")
    public ResponseEntity<Transaccion> registrarSimple(@RequestBody SimpleTransaccionRequest request) {
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()
                || request.getMonto() <= 0 || request.getTipo() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(servicio.registrarSimple(
                request.getDescripcion().trim(), request.getMonto(), request.getTipo()));
    }

    @GetMapping("/transacciones")
    public ResponseEntity<List<Transaccion>> obtenerTodas() {
        return ResponseEntity.ok(servicio.obtenerTodas());
    }

    @GetMapping("/transacciones/{id}")
    public ResponseEntity<Transaccion> obtenerPorId(@PathVariable Long id) {
        return servicio.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/transacciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return servicio.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/saldo")
    public ResponseEntity<Map<String, Double>> saldo() {
        return ResponseEntity.ok(Map.of("saldo", servicio.calcularSaldo()));
    }

    @GetMapping("/saldo/mes-actual")
    public ResponseEntity<Map<String, Double>> saldoMesActual() {
        return ResponseEntity.ok(Map.of("saldo", servicio.calcularSaldoMesActual()));
    }

    @GetMapping("/resumen/mes-actual")
    public ResponseEntity<Map<String, Object>> resumenMensual() {
        return ResponseEntity.ok(servicio.obtenerResumenMensual());
    }
}
```

El endpoint `/transacciones/simple` recibe un DTO ligero en lugar del objeto `Transaccion` completo. Antes de procesar, valida que los campos obligatorios no sean nulos ni vacíos y devuelve `400 Bad Request` si algo falla.

**`controller/dto/SimpleTransaccionRequest.java`**

```java
package com.finanzapi.controller.dto;

import com.finanzapi.model.TipoTransaccion;

public class SimpleTransaccionRequest {
    private String descripcion;
    private double monto;
    private TipoTransaccion tipo;

    // getters y setters...
}
```

El DTO evita exponer directamente la entidad JPA en la entrada de la API. Si la entidad cambia internamente (p. ej., se añaden campos gestionados por Hibernate), el contrato de la API no cambia.

---

## Paso 7 — El frontend estático

En lugar de Thymeleaf u otro motor de plantillas, se sirve un único `index.html` estático desde `src/main/resources/static/`. Spring Boot lo sirve automáticamente en la raíz `/`.

**`resources/static/index.html`**

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Finanz API</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 600px; margin: 2rem auto; }
        .endpoint { background: #f6f8fa; padding: 0.5rem 1rem; margin: 0.5rem 0; border-radius: 6px; }
    </style>
</head>
<body>
    <h1>Finanz API</h1>
    <p>API REST para gestión de presupuesto personal.</p>

    <div class="endpoint"><a href="/api/saldo">GET /api/saldo</a> — Saldo total</div>
    <div class="endpoint"><a href="/api/saldo/mes-actual">GET /api/saldo/mes-actual</a> — Saldo del mes</div>
    <div class="endpoint"><a href="/api/resumen/mes-actual">GET /api/resumen/mes-actual</a> — Resumen mensual</div>
    <div class="endpoint"><a href="/api/transacciones">GET /api/transacciones</a> — Listar transacciones</div>

    <p><small>Para registrar: <code>POST /api/transacciones/simple</code> con curl o Postman.</small></p>
</body>
</html>
```

---

## Paso 8 — Endpoints disponibles y cómo probarlos

### Listar todas las transacciones

```bash
curl http://localhost:8080/api/transacciones
```

### Registrar una transacción (forma simple)

```bash
curl -X POST http://localhost:8080/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Nómina", "monto": 1500.0, "tipo": "INGRESO"}'
```

### Consultar saldo total

```bash
curl http://localhost:8080/api/saldo
# → {"saldo": 800.0}
```

### Consultar resumen del mes actual

```bash
curl http://localhost:8080/api/resumen/mes-actual
```

### Eliminar una transacción por ID

```bash
curl -X DELETE http://localhost:8080/api/transacciones/1
# → 204 No Content si existe, 404 Not Found si no
```

---

## Paso 9 — Dockerizar la aplicación

El `Dockerfile` usa un **build en dos etapas** para mantener la imagen final pequeña.

**`Dockerfile`**

```dockerfile
# Etapa 1: compilar
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests -Djava.version=21 -B

# Etapa 2: imagen de producción (solo JRE, sin JDK)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

La primera etapa (`eclipse-temurin:21-jdk-alpine`) compila el proyecto con Maven. La segunda etapa usa solo el JRE (más ligero que el JDK) y copia únicamente el JAR compilado. El usuario `spring` no-root es una buena práctica de seguridad en contenedores.

```bash
# Construir la imagen
docker build -t finanz-api .

# Ejecutar
docker run -p 8080:8080 finanz-api
```

---

## Paso 10 — Configuración para cloud

El perfil `cloud` usa H2 en memoria en lugar de en disco, porque el sistema de archivos de los servicios cloud gratuitos (Render, Railway, Fly.io) suele ser efímero: los ficheros se pierden al reiniciar el contenedor.

**`resources/application-cloud.properties`**

```properties
spring.datasource.url=jdbc:h2:mem:finanz-api
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Deshabilitar la consola H2 en producción (seguridad)
spring.h2.console.enabled=false
```

Para activar este perfil se pasa la variable de entorno `SPRING_PROFILES_ACTIVE=cloud` al desplegar.

Los archivos `render.yaml`, `railway.json` y `fly.toml` incluidos en el proyecto contienen la configuración específica de cada plataforma para desplegar con un solo comando.

---

## Tabla resumen de endpoints

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| `GET` | `/api/transacciones` | Listar todas | — |
| `GET` | `/api/transacciones/{id}` | Obtener por ID | — |
| `POST` | `/api/transacciones` | Registrar (entidad completa) | `Transaccion` JSON |
| `POST` | `/api/transacciones/simple` | Registrar (DTO simplificado) | `{descripcion, monto, tipo}` |
| `DELETE` | `/api/transacciones/{id}` | Eliminar | — |
| `GET` | `/api/saldo` | Saldo total acumulado | — |
| `GET` | `/api/saldo/mes-actual` | Saldo del mes en curso | — |
| `GET` | `/api/resumen/mes-actual` | Resumen detallado del mes | — |

---

## Diferencias clave respecto a la versión Finanz Core

| Aspecto | Consola (src) | API REST (Finanz API / `finanz-api`) |
|---------|---------------|----------------------|
| Persistencia | CSV manual con `BufferedWriter` | H2 + JPA/Hibernate |
| Repositorio | Clase escrita a mano | Interfaz `JpaRepository` |
| IDs | `int` gestionado manualmente | `Long` autoincrement SQL |
| Entrada de datos | `Scanner` en terminal | JSON por HTTP |
| Gestión de dependencias | Ninguna (Java puro) | Maven + Spring Boot |
| Despliegue | JAR ejecutado en local | Docker + cloud (Render/Railway/Fly) |
| Tests | Framework propio sin librerías | Spring Boot Test + JUnit 5 |

---

## Decisiones de diseño destacadas

- **H2 embebida**: no requiere instalar ninguna base de datos. En desarrollo persiste en disco; en cloud cambia a memoria con un perfil. El mismo código funciona en los dos entornos.
- **DTO `SimpleTransaccionRequest`**: separa el contrato de la API de la entidad JPA. Si Hibernate añade campos internos a `Transaccion`, el cliente de la API no se ve afectado.
- **Validación en el controlador**: se valida antes de llamar al servicio. El servicio no debería recibir datos incorrectos.
- **`ResponseEntity` en todos los endpoints**: permite devolver el código HTTP adecuado (200, 204, 400, 404) en cada situación, no siempre un 200.
- **Dos etapas en Docker**: imagen final mucho más pequeña y sin el JDK de compilación.
- **`application-cloud.properties` como perfil separado**: permite mantener una configuración de desarrollo cómoda (H2 en disco, consola habilitada) sin que interfiera con la de producción.
