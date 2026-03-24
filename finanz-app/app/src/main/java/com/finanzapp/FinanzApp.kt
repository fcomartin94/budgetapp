package com.finanzapp

import android.app.Application
import com.finanzapp.data.local.AppDatabase
import com.finanzapp.data.local.TransaccionRepository

class FinanzApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transaccionRepository by lazy { TransaccionRepository(database.transaccionDao()) }
}
