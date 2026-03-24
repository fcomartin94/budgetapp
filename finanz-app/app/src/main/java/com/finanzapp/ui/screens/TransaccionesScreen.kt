package com.finanzapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
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
        contentWindowInsets = WindowInsets.safeDrawing,
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
