# Finanz App (`finanz-app/`)

Aplicacion Android nativa para control financiero personal, desarrollada con Kotlin, Jetpack Compose y Room.

---

## Indice

- [Finanz App (`finanz-app/`)](#finanz-app-finanz-app)
  - [Indice](#indice)
  - [1. Estado real actual](#1-estado-real-actual)
  - [2. Vision funcional](#2-vision-funcional)
  - [3. Stack tecnico](#3-stack-tecnico)
  - [4. Arquitectura de la app](#4-arquitectura-de-la-app)
  - [5. Modelo de datos y persistencia](#5-modelo-de-datos-y-persistencia)
    - [Entidad principal `Transaccion`](#entidad-principal-transaccion)
    - [Categoria](#categoria)
    - [DAO](#dao)
  - [6. Navegacion y pantallas](#6-navegacion-y-pantallas)
    - [Home](#home)
    - [Transacciones](#transacciones)
    - [Resumen](#resumen)
  - [7. Manual de uso](#7-manual-de-uso)
    - [7.1 Registrar un movimiento](#71-registrar-un-movimiento)
    - [7.2 Consultar salud financiera](#72-consultar-salud-financiera)
    - [7.3 Corregir datos](#73-corregir-datos)
  - [8. Comportamiento responsive y UI](#8-comportamiento-responsive-y-ui)
  - [9. Compilacion e instalacion](#9-compilacion-e-instalacion)
    - [Requisitos](#requisitos)
    - [Ejecutar desde Android Studio](#ejecutar-desde-android-studio)
    - [Generar APK debug](#generar-apk-debug)
  - [10. Limitaciones actuales](#10-limitaciones-actuales)
  - [11. Relacion con `finanz-core` y `finanz-api`](#11-relacion-con-finanz-core-y-finanz-api)
  - [12. Roadmap recomendado](#12-roadmap-recomendado)

---

## 1. Estado real actual

Estado funcional actual:

- App operativa con almacenamiento local Room.
- Formulario simplificado para alta de movimientos.
- UI modernizada (Material 3, gradientes, tarjetas KPI, diferenciacion ingreso/gasto).
- Ajustes de insets para dispositivos reales (barras del sistema y teclado).

Persistencia:

- Base de datos local: `finanzapp_db` (Room / SQLite).

---

## 2. Vision funcional

La app permite:

- Registrar movimientos (ingreso o gasto) con:
  - descripcion
  - monto
  - tipo
- Ver saldo total.
- Ver saldo del mes actual.
- Ver historial de movimientos.
- Ver resumen mensual con:
  - ingresos
  - gastos
  - balance
  - proporcion gastos/ingresos
  - movimientos recientes
- Eliminar movimientos desde historial.

Importante:

- Categoria y limite ya no se capturan en UI.
- Internamente se guarda categoria por defecto (`General`) para compatibilidad de dominio.

---

## 3. Stack tecnico

| Componente | Valor |
| --- | --- |
| Lenguaje | Kotlin `1.9.24` |
| UI | Jetpack Compose + Material 3 |
| Navegacion | Navigation Compose |
| Persistencia | Room `2.6.1` + SQLite |
| Arquitectura | MVVM + capas (`data`, `domain`, `ui`) |
| minSdk | 26 |
| targetSdk | 35 |
| compileSdk | 35 |
| Java/Kotlin target | 17 |

Plugins principales:

- `com.android.application` `8.5.2`
- `org.jetbrains.kotlin.android` `1.9.24`
- `com.google.devtools.ksp` `1.9.24-1.0.20`

---

## 4. Arquitectura de la app

```text
com.finanzapp/
├── MainActivity.kt
├── FinanzApp.kt
├── data/
│   ├── model/
│   │   ├── Transaccion.kt
│   │   ├── TipoTransaccion.kt
│   │   └── Categoria.kt
│   └── local/
│       ├── AppDatabase.kt
│       ├── TransaccionDao.kt
│       ├── TransaccionRepository.kt
│       └── Converters.kt
├── domain/
│   └── BudgetService.kt
└── ui/
    ├── navigation/FinanzAppNavHost.kt
    ├── viewmodel/BudgetViewModel.kt
    ├── screens/
    │   ├── HomeScreen.kt
    │   ├── TransaccionesScreen.kt
    │   └── ResumenScreen.kt
    └── theme/Theme.kt
```

Flujo de datos:

`UI (Compose) -> ViewModel -> BudgetService -> Repository -> DAO -> Room`

Puntos clave:

- `FinanzApp` crea DB y repositorio (inyeccion manual simple).
- `BudgetViewModel` expone `StateFlow<UiState>`.
- `Dao.obtenerTodas()` devuelve `Flow<List<Transaccion>>` para UI reactiva.

---

## 5. Modelo de datos y persistencia

### Entidad principal `Transaccion`

- `id: Long`
- `descripcion: String`
- `monto: Double`
- `tipo: TipoTransaccion`
- `fecha: LocalDate`
- `categoria: Categoria` (`@Embedded(prefix = "cat_")`)

### Categoria

Aunque la UI no pide categoria/limite, el modelo lo conserva:

- `nombre: String`
- `presupuestoLimite: Double`

En altas desde UI:

- `nombre = "General"`
- `presupuestoLimite = 0.0`

Esto mantiene compatibilidad de dominio con otras versiones del repositorio.

### DAO

- Listado completo (Flow) ordenado por fecha descendente.
- Filtro por tipo.
- Filtro por rango de fechas.
- Insercion y borrado por ID.

---

## 6. Navegacion y pantallas

Rutas en `FinanzAppNavHost.kt`:

- `home`
- `transacciones`
- `resumen`

### Home

- Tarjeta principal con saldo disponible.
- KPI de saldo del mes y numero de movimientos.
- Estado vacio cuando no hay movimientos.
- Acceso rapido a Transacciones y Resumen.

### Transacciones

- Formulario simplificado:
  - descripcion
  - monto
  - chip Ingreso/Gasto
- Boton "Guardar movimiento".
- Historial con diferenciacion visual fuerte:
  - chip `INGRESO` o `GASTO`
  - color de tarjeta
  - barra lateral de color
  - signo en importe (`+` / `-`)
- Accion de borrado por item.

### Resumen

- Encabezado mensual.
- KPI de ingresos y gastos.
- Balance del mes.
- Indicador de proporcion de gasto sobre ingresos.
- Lista de movimientos recientes del mes.

---

## 7. Manual de uso

### 7.1 Registrar un movimiento

1. Abrir `Transacciones`.
2. Completar:
   - descripcion
   - monto (> 0)
   - tipo (Ingreso/Gasto)
3. Pulsar `Guardar movimiento`.

### 7.2 Consultar salud financiera

1. En `Inicio` revisar saldo total y saldo del mes.
2. En `Resumen` revisar:
   - ingresos
   - gastos
   - balance
   - proporcion de gasto

### 7.3 Corregir datos

- En `Transacciones`, borrar movimientos con icono de papelera.

---

## 8. Comportamiento responsive y UI

La app usa:

- `Scaffold` con `WindowInsets.safeDrawing`.
- `navigationBarsPadding()` y `imePadding()`.
- `LazyColumn` / `verticalScroll` segun pantalla.

Objetivo:

- evitar recortes en dispositivos reales
- mantener accesibles contenido y acciones con teclado abierto

UI/tema:

- Material 3 con esquema dinamico (Android 12+).
- Paleta custom fallback.
- `Shapes` redondeadas globales.
- Fondos degradados suaves y tarjetas elevadas.

---

## 9. Compilacion e instalacion

### Requisitos

- Android Studio actual.
- JDK 17 para build estable con AGP/Kotlin de este proyecto.

### Ejecutar desde Android Studio

1. Abrir carpeta `finanz-app`.
2. Sincronizar Gradle.
3. Ejecutar configuracion `app`.

### Generar APK debug

```bash
cd finanz-app
./gradlew assembleDebug
```

APK esperado:

- `app/build/outputs/apk/debug/app-debug.apk`

Instalacion por ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 10. Limitaciones actuales

- No hay edicion de transacciones (solo alta y borrado).
- No hay filtros avanzados en historial.
- No hay exportacion de datos.
- No hay sincronizacion en nube (offline local).
- Formato de importe no totalmente homogeno en todas las vistas (`€` y `EUR` coexisten).

---

## 11. Relacion con `finanz-core` y `finanz-api`

- Comparte dominio financiero base con ambas implementaciones.
- Android prioriza UX simplificada de captura.
- Finanz API (`finanz-api/`) incorpora endpoint simplificado equivalente.
- `finanz-core` conserva enfoque CLI didactico (actualmente con errores pendientes en codigo).

---

## 12. Roadmap recomendado

- Homogeneizar formato monetario en toda la UI.
- Anadir filtros por fecha/tipo en historial.
- Incorporar edicion de movimientos.
- Exportar/importar datos.
- Mejorar validaciones de formulario (mensajes inline).
- Preparar testing UI/instrumentado.
