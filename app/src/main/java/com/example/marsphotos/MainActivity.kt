package com.example.marsphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.marsphotos.sync.ExchangeRateWorker
import com.example.marsphotos.ui.CurrencyScreen
import com.example.marsphotos.ui.CurrencyViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    // Usamos viewModels para obtener la instancia de CurrencyViewModel
    private val currencyViewModel: CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear un WorkRequest para actualizar cada hora
        val workRequest = PeriodicWorkRequestBuilder<ExchangeRateWorker>(1, TimeUnit.HOURS)
            .build()

        // Enviar el WorkRequest al WorkManager
        WorkManager.getInstance(this).enqueue(workRequest)

        setContent {
            MaterialTheme {
                Surface {
                     CurrencyScreen(viewModel = currencyViewModel)
                }
            }
        }
    }
}
