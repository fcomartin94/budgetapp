package com.finanzapp

import android.app.Application
import com.finanzapp.data.local.AppDatabase
import com.finanzapp.data.local.TransaccionRepository

/**
 * Custom [Application] class that acts as the manual dependency injection root.
 *
 * Creates the Room database and the [TransaccionRepository] as lazy singletons,
 * making them available to [com.finanzapp.ui.viewmodel.BudgetViewModel] via
 * `application as FinanzApp`. Both are initialised only once per process lifetime.
 */
class FinanzApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transaccionRepository by lazy { TransaccionRepository(database.transaccionDao()) }
}
