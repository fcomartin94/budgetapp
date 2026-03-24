# Como se hizo: Finanz App desde 0 (guia reproducible)

> Esta version esta escrita para reconstruir la app completa desde cero, paso a paso, sin depender de mirar el codigo fuente original.

---

## Objetivo

Construir una app Android nativa con:

- Kotlin
- Jetpack Compose (Material 3)
- Room (SQLite local)
- Arquitectura MVVM simple por capas

Funcionalidades finales:

- Registrar ingresos y gastos
- Ver saldo total
- Ver saldo del mes actual
- Ver historial
- Ver resumen mensual
- Eliminar movimientos

---

## 0) Requisitos

- Android Studio (recomendado Hedgehog o mas nuevo)
- JDK 17
- SDK Android con API 35 instalada
- Emulador o dispositivo fisico para pruebas

Comprobar Java:

```bash
java -version
```

Debe reportar Java 17.x.

---

## 1) Crear proyecto base en Android Studio

### Por que esta decision

- `Empty Activity` reduce ruido inicial y evita borrar codigo generado que no aporta al caso.
- `minSdk 26` permite APIs modernas de Kotlin/Android manteniendo buena compatibilidad.
- `package com.finanzapp` alinea namespace, imports y estructura en todas las capas.
- Arrancar desde template oficial baja riesgo de errores de configuracion "invisible".

1. `File -> New -> New Project`.
2. Plantilla: `Empty Activity`.
3. Configurar:
   - Name: `FinanzApp`
   - Package name: `com.finanzapp`
   - Language: `Kotlin`
   - Minimum SDK: `API 26`
4. Finalizar y esperar sincronizacion Gradle.

Al terminar, ejecuta una vez la app base para validar entorno (debe abrir una pantalla vacia o demo inicial).

---

## 2) Configurar Gradle raiz

### Por que esta decision

- Fijar versiones en raiz centraliza el control y evita desalineaciones entre modulos.
- `KSP` se declara desde el inicio porque Room depende de generacion de codigo.
- Versiones explicitas hacen el build mas reproducible (menos "me funciona en mi maquina").

Archivo: `build.gradle.kts` (raiz del modulo Android)

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
```

Checkpoint:

- Sync Gradle sin errores.

---

## 3) Configurar modulo app

### Por que esta decision

- `compileSdk/targetSdk 35` asegura acceso a APIs recientes y comportamiento esperado en Android actual.
- `Java/Kotlin 17` evita incompatibilidades con AGP moderno y librerias actuales.
- Compose + Material3 permiten UI declarativa: menos estado manual y menos XML imperative.
- Se evita `minify` en debug/release inicial para facilitar depuracion; optimizacion puede venir despues.

Archivo: `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.finanzapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.finanzapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

Checkpoint:

- Sync Gradle.
- Sin errores de version de Kotlin/Compose.

---

## 4) Crear estructura de paquetes

### Por que esta decision

- Separar `data`, `domain`, `ui` reduce acoplamiento y facilita pruebas y evolucion.
- `navigation` y `viewmodel` como subcapas evitan que la UI crezca sin orden.
- La regla principal: la UI no toca Room directamente; pasa por ViewModel/servicio.

En `finanz-app/app/src/main/java/com/finanzapp/` crea:

- `data/model`
- `data/local`
- `domain`
- `ui/navigation`
- `ui/viewmodel`
- `ui/screens`
- `ui/theme`

---

## 5) Modelo de datos (domain/data model)

### Por que esta decision

- `TipoTransaccion` como enum evita strings magicos y errores tipograficos.
- `Transaccion` como `@Entity` mapea 1:1 a persistencia y simplifica consultas.
- `LocalDate` (y no `LocalDateTime`) encaja con el problema: resumen diario/mensual.
- `Categoria` se mantiene por compatibilidad de dominio aunque el formulario sea simple.

### `data/model/TipoTransaccion.kt`

```kotlin
package com.finanzapp.data.model

enum class TipoTransaccion {
    INGRESO,
    GASTO
}
```

### `data/model/Categoria.kt`

```kotlin
package com.finanzapp.data.model

data class Categoria(
    val nombre: String,
    val presupuestoLimite: Double
)
```

### `data/model/Transaccion.kt`

```kotlin
package com.finanzapp.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoTransaccion,
    val fecha: LocalDate,
    @Embedded(prefix = "cat_")
    val categoria: Categoria
)
```

---

## 6) Persistencia con Room

### Por que esta decision

- Room agrega seguridad de tipos sobre SQLite y evita SQL manual disperso.
- DAO con `Flow` da reactividad nativa: al insertar/borrar, la UI se actualiza sola.
- `Repository` encapsula datos para que la capa superior no dependa de detalles DAO.
- `TypeConverter` resuelve tipos no nativos de SQLite (fecha y enum) sin hacks.

### `data/local/Converters.kt`

```kotlin
package com.finanzapp.data.local

import androidx.room.TypeConverter
import com.finanzapp.data.model.TipoTransaccion
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromTipoTransaccion(value: TipoTransaccion): String = value.name

    @TypeConverter
    fun toTipoTransaccion(value: String): TipoTransaccion = TipoTransaccion.valueOf(value)
}
```

### `data/local/TransaccionDao.kt`

```kotlin
package com.finanzapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransaccionDao {

    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Transaccion>>

    @Query("SELECT * FROM transacciones WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Transaccion?

    @Query("SELECT * FROM transacciones WHERE tipo = :tipo")
    suspend fun obtenerPorTipo(tipo: TipoTransaccion): List<Transaccion>

    @Query("SELECT * FROM transacciones WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun obtenerEntreFechas(inicio: LocalDate, fin: LocalDate): List<Transaccion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(transaccion: Transaccion): Long

    @Delete
    suspend fun eliminar(transaccion: Transaccion)

    @Query("DELETE FROM transacciones WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}
```

### `data/local/AppDatabase.kt`

```kotlin
package com.finanzapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finanzapp.data.model.Transaccion

@Database(entities = [Transaccion::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transaccionDao(): TransaccionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzapp_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### `data/local/TransaccionRepository.kt`

```kotlin
package com.finanzapp.data.local

import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class TransaccionRepository(private val dao: TransaccionDao) {

    val transacciones: Flow<List<Transaccion>> = dao.obtenerTodas()

    suspend fun insertar(transaccion: Transaccion): Long = dao.insertar(transaccion)

    suspend fun eliminar(id: Long) = dao.eliminarPorId(id)

    suspend fun obtenerTodas(): List<Transaccion> = dao.obtenerTodas().first()

    suspend fun obtenerPorTipo(tipo: TipoTransaccion): List<Transaccion> =
        dao.obtenerPorTipo(tipo)

    suspend fun obtenerEntreFechas(inicio: LocalDate, fin: LocalDate): List<Transaccion> =
        dao.obtenerEntreFechas(inicio, fin)
}
```

Checkpoint:

- Build del proyecto (`Build -> Make Project`) sin errores.

---

## 7) Capa de dominio (reglas de negocio)

### Por que esta decision

- `BudgetService` concentra reglas de calculo (saldo, resumen) fuera de UI.
- Si el calculo cambia, se toca un sitio; no tres pantallas distintas.
- El servicio decide defaults de negocio (ej. categoria "General"), no la vista.

### `domain/BudgetService.kt`

```kotlin
package com.finanzapp.domain

import com.finanzapp.data.local.TransaccionRepository
import com.finanzapp.data.model.Categoria
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import java.time.LocalDate

class BudgetService(private val repositorio: TransaccionRepository) {

    suspend fun registrarTransaccion(
        descripcion: String,
        monto: Double,
        tipo: TipoTransaccion
    ): Long {
        val transaccion = Transaccion(
            descripcion = descripcion,
            monto = monto,
            tipo = tipo,
            fecha = LocalDate.now(),
            categoria = Categoria(
                nombre = "General",
                presupuestoLimite = 0.0
            )
        )
        return repositorio.insertar(transaccion)
    }

    suspend fun calcularSaldo(): Double {
        val todas = repositorio.obtenerTodas()
        val ingresos = todas.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = todas.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ingresos - gastos
    }

    suspend fun calcularSaldoMesActual(): Double {
        val inicio = LocalDate.now().withDayOfMonth(1)
        val fin = LocalDate.now()
        val transacciones = repositorio.obtenerEntreFechas(inicio, fin)
        val ingresos = transacciones.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = transacciones.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ingresos - gastos
    }

    suspend fun obtenerResumenMensual(): ResumenMensual {
        val inicio = LocalDate.now().withDayOfMonth(1)
        val fin = LocalDate.now()
        val transacciones = repositorio.obtenerEntreFechas(inicio, fin)
        val ingresos = transacciones.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = transacciones.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ResumenMensual(
            mes = LocalDate.now().monthValue,
            anio = LocalDate.now().year,
            ingresos = ingresos,
            gastos = gastos,
            saldo = ingresos - gastos,
            transacciones = transacciones
        )
    }

    suspend fun eliminarTransaccion(id: Long) = repositorio.eliminar(id)
}

data class ResumenMensual(
    val mes: Int,
    val anio: Int,
    val ingresos: Double,
    val gastos: Double,
    val saldo: Double,
    val transacciones: List<Transaccion>
)
```

---

## 8) Application + ViewModel

### Por que esta decision

- `Application` sirve para bootstrap de dependencias singleton sin framework de DI.
- `AndroidViewModel` facilita acceso controlado al `Application` para construir servicios.
- `UiState` unico evita estados fragmentados y bugs de sincronizacion entre pantallas.
- `StateFlow` encaja con Compose y minimiza callbacks manuales.

### `FinanzApp.kt`

```kotlin
package com.finanzapp

import android.app.Application
import com.finanzapp.data.local.AppDatabase
import com.finanzapp.data.local.TransaccionRepository

class FinanzApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transaccionRepository by lazy { TransaccionRepository(database.transaccionDao()) }
}
```

### `ui/viewmodel/BudgetViewModel.kt`

```kotlin
package com.finanzapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.finanzapp.FinanzApp
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import com.finanzapp.domain.BudgetService
import com.finanzapp.domain.ResumenMensual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val transacciones: List<Transaccion> = emptyList(),
    val saldoTotal: Double = 0.0,
    val saldoMesActual: Double = 0.0,
    val resumenMensual: ResumenMensual? = null,
    val isLoading: Boolean = false
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FinanzApp
    private val budgetService = BudgetService(app.transaccionRepository)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
        observarTransacciones()
    }

    private fun observarTransacciones() {
        viewModelScope.launch {
            app.transaccionRepository.transacciones.collect { transacciones ->
                _uiState.update { it.copy(transacciones = transacciones) }
                actualizarCalculos()
            }
        }
    }

    private suspend fun actualizarCalculos() {
        val saldoTotal = budgetService.calcularSaldo()
        val saldoMesActual = budgetService.calcularSaldoMesActual()
        val resumenMensual = budgetService.obtenerResumenMensual()
        _uiState.update {
            it.copy(
                saldoTotal = saldoTotal,
                saldoMesActual = saldoMesActual,
                resumenMensual = resumenMensual
            )
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            actualizarCalculos()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun registrarTransaccion(
        descripcion: String,
        monto: Double,
        tipo: TipoTransaccion
    ) {
        viewModelScope.launch {
            budgetService.registrarTransaccion(
                descripcion = descripcion,
                monto = monto,
                tipo = tipo
            )
        }
    }

    fun eliminarTransaccion(id: Long) {
        viewModelScope.launch {
            budgetService.eliminarTransaccion(id)
        }
    }
}
```

---

## 9) Navegacion

### Por que esta decision

- Navigation Compose evita manejar back stack manual.
- Rutas constantes (`Routes`) eliminan strings repetidos y errores de typo.
- Se pasa el ViewModel como fuente unica de estado para mantener consistencia.

### `ui/navigation/FinanzAppNavHost.kt`

```kotlin
package com.finanzapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finanzapp.ui.screens.HomeScreen
import com.finanzapp.ui.screens.ResumenScreen
import com.finanzapp.ui.screens.TransaccionesScreen
import com.finanzapp.ui.viewmodel.BudgetViewModel

object Routes {
    const val HOME = "home"
    const val TRANSACCIONES = "transacciones"
    const val RESUMEN = "resumen"
}

@Composable
fun FinanzAppNavHost(viewModel: BudgetViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                uiState = viewModel.uiState,
                onNavigateToTransacciones = { navController.navigate(Routes.TRANSACCIONES) },
                onNavigateToResumen = { navController.navigate(Routes.RESUMEN) }
            )
        }
        composable(Routes.TRANSACCIONES) {
            TransaccionesScreen(
                uiState = viewModel.uiState,
                onRegistrarTransaccion = viewModel::registrarTransaccion,
                onEliminarTransaccion = viewModel::eliminarTransaccion,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.RESUMEN) {
            ResumenScreen(
                uiState = viewModel.uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 10) Theme

### Por que esta decision

- Centralizar tema evita estilos "hardcoded" repartidos por pantallas.
- Dynamic color mejora integracion visual en Android 12+ sin logica adicional.
- Shapes y paleta comunes hacen la UI coherente con poco esfuerzo.

### `ui/theme/Theme.kt`

```kotlin
package com.finanzapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val BluePrimary = Color(0xFF3366FF)
private val BlueLight = Color(0xFF8FA8FF)
private val BlueDark = Color(0xFF0E1B4D)
private val TealSecondary = Color(0xFF00A8A8)
private val TealDark = Color(0xFF005F63)

private val DarkColorScheme = darkColorScheme(
    primary = BlueLight,
    onPrimary = BlueDark,
    secondary = TealSecondary,
    onSecondary = Color.White,
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE6EDF3)
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = TealDark,
    onSecondary = Color.White,
    background = Color(0xFFF5F7FF),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun FinanzAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalView.current.context
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content
    )
}
```

---

## 11) Pantallas Compose

### Por que esta decision

- Tres pantallas separadas (Home/Transacciones/Resumen) reflejan tres tareas de usuario.
- Formularios simples bajan friccion de captura en movil.
- `Scaffold + safeDrawing + imePadding` prioriza usabilidad real (teclado y barras del sistema).
- Las pantallas no calculan finanzas; solo renderizan estado y emiten eventos.

### `ui/screens/HomeScreen.kt`

```kotlin
package com.finanzapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzapp.ui.viewmodel.UiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: StateFlow<UiState>,
    onNavigateToTransacciones: () -> Unit,
    onNavigateToResumen: () -> Unit
) {
    val state = uiState.collectAsStateWithLifecycle().value

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FinanzApp",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel financiero personal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Saldo disponible", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "%.2f €".format(state.saldoTotal),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(
                                    if (state.saldoTotal >= 0.0) "Balance positivo"
                                    else "Balance en negativo"
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFEAF2FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mes actual", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%.2f €".format(state.saldoMesActual),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Movimientos", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.transacciones.size.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (state.transacciones.isEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Aun no hay movimientos", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Empieza registrando tu primer ingreso o gasto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onNavigateToTransacciones,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Transacciones")
                    }
                    Button(
                        onClick = onNavigateToResumen,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resumen")
                    }
                }
            }
        }
    }
}
```

### `ui/screens/TransaccionesScreen.kt`

```kotlin
package com.finanzapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import com.finanzapp.ui.viewmodel.UiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaccionesScreen(
    uiState: StateFlow<UiState>,
    onRegistrarTransaccion: (String, Double, TipoTransaccion) -> Unit,
    onEliminarTransaccion: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state = uiState.collectAsStateWithLifecycle().value

    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoTransaccion.GASTO) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Transacciones") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Nuevo movimiento",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Registro rapido: descripcion, monto y tipo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripcion") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tipo == TipoTransaccion.INGRESO,
                            onClick = { tipo = TipoTransaccion.INGRESO },
                            label = { Text("Ingreso") },
                            leadingIcon = { Text("+") }
                        )
                        FilterChip(
                            selected = tipo == TipoTransaccion.GASTO,
                            onClick = { tipo = TipoTransaccion.GASTO },
                            label = { Text("Gasto") },
                            leadingIcon = { Text("-") }
                        )
                    }
                }
                item {
                    Button(
                        onClick = {
                            val m = monto.toDoubleOrNull() ?: 0.0
                            if (descripcion.isNotBlank() && m > 0.0) {
                                onRegistrarTransaccion(descripcion, m, tipo)
                                descripcion = ""
                                monto = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar movimiento")
                    }
                }
                item {
                    Text(
                        "Historial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (state.transacciones.isEmpty()) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Sin historial por ahora", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Cuando registres movimientos apareceran aqui",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                items(state.transacciones, key = { it.id }) { t ->
                    TransaccionItem(
                        transaccion = t,
                        onDelete = { onEliminarTransaccion(t.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransaccionItem(
    transaccion: Transaccion,
    onDelete: () -> Unit
) {
    val isIngreso = transaccion.tipo == TipoTransaccion.INGRESO
    val amountColor = if (isIngreso) Color(0xFF1B8A3A) else MaterialTheme.colorScheme.error
    val amountPrefix = if (isIngreso) "+" else "-"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isIngreso)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(if (isIngreso) Color(0xFF1B8A3A) else MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(if (isIngreso) "INGRESO" else "GASTO") },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = if (isIngreso) {
                            Color(0xFFE6F6EA)
                        } else {
                            Color(0xFFFDECEC)
                        },
                        disabledLabelColor = if (isIngreso) {
                            Color(0xFF1B8A3A)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = transaccion.descripcion,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaccion.fecha.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "$amountPrefix %.2f EUR".format(transaccion.monto),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar")
            }
        }
    }
}
```

### `ui/screens/ResumenScreen.kt`

```kotlin
package com.finanzapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.ui.viewmodel.UiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenScreen(
    uiState: StateFlow<UiState>,
    onNavigateBack: () -> Unit
) {
    val state = uiState.collectAsStateWithLifecycle().value
    val resumen = state.resumenMensual
    val ratioGastos = if (resumen == null || resumen.ingresos <= 0.0) {
        0f
    } else {
        (resumen.gastos / resumen.ingresos).coerceIn(0.0, 1.0).toFloat()
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Resumen mensual") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (resumen != null) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Resumen ${resumen.mes}/${resumen.anio}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pulso financiero mensual",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "Ingresos",
                            amount = resumen.ingresos,
                            amountColor = Color(0xFF1B8A3A),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Gastos",
                            amount = resumen.gastos,
                            amountColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Balance del mes", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "%.2f €".format(resumen.saldo),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Proporcion de gastos sobre ingresos")
                            LinearProgressIndicator(
                                progress = ratioGastos,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Text(
                        "Movimientos recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (resumen.transacciones.isEmpty()) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = "Aun no hay movimientos en este mes.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        resumen.transacciones
                            .take(5)
                            .forEach { transaccion ->
                                val isIngreso = transaccion.tipo == TipoTransaccion.INGRESO
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = if (isIngreso) {
                                            Color(0xFFEAF7EF)
                                        } else {
                                            Color(0xFFFFF0F0)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                transaccion.descripcion,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                transaccion.fecha.toString(),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            text = (if (isIngreso) "+ " else "- ") + "%.2f €".format(transaccion.monto),
                                            color = if (isIngreso) Color(0xFF1B8A3A) else MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                    }
                } else {
                    Text("Cargando resumen...")
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    amount: Double,
    amountColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "%.2f €".format(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
```

> Nota: en este archivo no uses `KpiCardDefaults`; no existe en Material3 y rompe compilacion.

---

## 12) Activity y Manifest

### Por que esta decision

- `enableEdgeToEdge()` aprovecha toda la pantalla y moderniza UX.
- Manifest define `FinanzApp` como `Application` para inicializar DB/repo antes de UI.
- `MainActivity` queda fina: aplica tema y delega navegacion/estado.

### `MainActivity.kt`

```kotlin
package com.finanzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzapp.ui.navigation.FinanzAppNavHost
import com.finanzapp.ui.theme.FinanzAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanzAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinanzAppNavHost(viewModel = viewModel())
                }
            }
        }
    }
}
```

### `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".FinanzApp"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher_foreground"
        android:label="@string/app_name"
        android:roundIcon="@drawable/ic_launcher_foreground"
        android:supportsRtl="true"
        android:theme="@style/Theme.FinanzApp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FinanzApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 13) Recursos XML minimos

### Por que esta decision

- Mantener XML minimo evita duplicar configuracion con Compose.
- `Theme.FinanzApp` base se usa para compatibilidad de arranque y system bars.
- Launcher assets por defecto son suficientes en fase funcional.

### `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">FinanzApp</string>
</resources>
```

### `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.FinanzApp" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">true</item>
    </style>
</resources>
```

### `app/src/main/res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#FFFFFF</color>
</resources>
```

Los archivos de launcher (`ic_launcher*.xml`, `ic_launcher_foreground.xml`) pueden quedarse con los generados por defecto del template.

---

## 14) Build y ejecucion final

Desde terminal:

```bash
cd finanz-app
./gradlew clean assembleDebug
```

APK esperado:

- `app/build/outputs/apk/debug/app-debug.apk`

Instalar en dispositivo conectado:

```bash
./gradlew installDebug
```

---

## 15) Check funcional rapido

1. Abrir app -> Home debe mostrar saldo `0.00`.
2. Ir a `Transacciones` -> crear:
   - `Sueldo`, `1500`, `Ingreso`
   - `Super`, `250`, `Gasto`
3. Volver a Home:
   - Saldo total debe ser `1250`.
4. Ir a `Resumen`:
   - Ingresos y gastos del mes deben reflejar esos valores.
5. Eliminar una transaccion desde historial:
   - KPI y saldo deben actualizarse automaticamente.

Si eso ocurre, reconstruiste la app correctamente.

---

## 16) Problemas comunes y solucion

- **Error de Java/Kotlin target**  
  Verifica JDK 17 en Android Studio (`Settings -> Build Tools -> Gradle -> Gradle JDK`).

- **Error Room + LocalDate**  
  Revisa que `@TypeConverters(Converters::class)` este en `AppDatabase`.

- **Error KSP no genera clases**  
  Comprueba plugin `com.google.devtools.ksp` en raiz y en `app/build.gradle.kts`.

- **Error de imports Material3**  
  Asegura dependencia `androidx.compose.material3:material3` y elimina imports inexistentes.

- **Compila pero crashea al iniciar**  
  Revisa `android:name=".FinanzApp"` en `AndroidManifest.xml`.

---

## 17) Resultado tecnico final

Arquitectura lograda:

`Compose UI -> BudgetViewModel -> BudgetService -> TransaccionRepository -> TransaccionDao -> Room (SQLite)`

Con esta guia puedes levantar desde cero la misma app Android y obtener el mismo comportamiento funcional.
