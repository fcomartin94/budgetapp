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
import com.finanzapp.ui.theme.FinanzAppTheme
import com.finanzapp.ui.navigation.FinanzAppNavHost

/**
 * Single activity that hosts the entire Compose UI.
 *
 * Enables edge-to-edge display and sets [FinanzAppNavHost] as the content root.
 * The [com.finanzapp.ui.viewmodel.BudgetViewModel] is created here via
 * `viewModel()` and passed down to the nav host, surviving configuration changes.
 */
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
