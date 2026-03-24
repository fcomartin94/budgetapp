# 📱 Finanz App

> Aplicacion Android nativa para control financiero personal, construida con **Kotlin + Jetpack Compose + Room**. Mantiene la misma logica de dominio de las versiones Finanz Core/Finanz API, pero optimizada para una experiencia movil moderna y reactiva.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fcomartin94/finanz-core)

---

## 🎯 ¿Que hace esta app?

Permite registrar movimientos financieros (ingresos y gastos), visualizarlos en tiempo real y consultar indicadores clave del mes, todo con persistencia local offline en SQLite.

**Funcionalidades principales:**
- ✅ Registrar ingresos y gastos con formulario simplificado
- ✅ Ver saldo total acumulado
- ✅ Ver saldo del mes actual
- ✅ Consultar historial de movimientos
- ✅ Ver resumen mensual (ingresos, gastos, balance y proporcion)
- ✅ Eliminar movimientos del historial
- ✅ Persistencia local automatica con Room

---

## 🏗️ Arquitectura

```text
┌───────────────────────────┐
│   UI (Jetpack Compose)    │  ← Screens + Navigation
├───────────────────────────┤
│   ViewModel (MVVM)        │  ← BudgetViewModel (StateFlow)
├───────────────────────────┤
│   Domain Service          │  ← BudgetService
├───────────────────────────┤
│   Repository + DAO        │  ← TransaccionRepository / TransaccionDao
├───────────────────────────┤
│   Room (SQLite local)     │  ← finanzapp_db
└───────────────────────────┘
```

| Capa | Responsabilidad |
|------|----------------|
| `ui` | Renderizado Compose, eventos de usuario y navegacion |
| `ui.viewmodel` | Estado reactivo y coordinacion de casos de uso |
| `domain` | Logica financiera: saldos, resumenes y reglas de negocio |
| `data.local` | Acceso a datos con Room (DAO, repositorio, base de datos) |
| `data.model` | Entidades de dominio (`Transaccion`, `TipoTransaccion`, `Categoria`) |

---

## 🧰 Stack tecnico

| Tecnologia | Detalles |
|------------|----------|
| **Kotlin** | 1.9.24 |
| **Jetpack Compose** | UI declarativa + Material 3 |
| **Navigation Compose** | Navegacion entre pantallas (`home`, `transacciones`, `resumen`) |
| **Room** | 2.6.1 con SQLite local |
| **Arquitectura** | MVVM + capas (`ui`, `domain`, `data`) |
| **Android SDK** | minSdk 26 / targetSdk 35 / compileSdk 35 |
| **Build** | Gradle Kotlin DSL + KSP |

---

## 📱 Pantallas principales

### Home

- Saldo disponible total
- KPI de saldo mensual y cantidad de movimientos
- Accesos directos al historial y al resumen

### Transacciones

- Formulario de alta: descripcion, monto y tipo (Ingreso/Gasto)
- Historial con diferenciacion visual por tipo
- Borrado de movimientos por item

### Resumen

- Totales del mes (ingresos, gastos, balance)
- Indicador de proporcion gasto/ingreso
- Lista de movimientos recientes del mes

---

## 🚀 Como ejecutar

### Requisitos

- Android Studio actualizado
- JDK 17

### Ejecutar en emulador o dispositivo

1. Abrir la carpeta `finanz-app` en Android Studio
2. Sincronizar Gradle
3. Ejecutar configuracion `app`

### Generar APK debug por terminal

```bash
cd finanz-app
./gradlew assembleDebug
```

APK generado:

`app/build/outputs/apk/debug/app-debug.apk`

---

## 💡 Decisiones de diseño destacadas

- **UI reactiva con `Flow`/`StateFlow`**: cualquier cambio en Room se refleja automaticamente en las pantallas sin refrescos manuales.
- **MVVM con separacion real de capas**: la UI no calcula balances; delega al `BudgetViewModel` y al `BudgetService`.
- **Persistencia offline-first**: toda la app funciona sin red al apoyarse en SQLite local.
- **Formulario simplificado**: categoria y limite se abstraen de la captura para priorizar velocidad de registro en movil.
- **Compatibilidad de dominio**: aunque la UI es simple, se conserva el modelo compartido con las otras versiones del proyecto.

---

## 📁 Estructura del proyecto

```text
finanz-app/app/src/main/java/com/finanzapp/
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

---

> Esta app es la implementacion Android del ecosistema FinanzApp, junto con las versiones CLI y API REST, compartiendo la misma base funcional de gestion financiera personal.
