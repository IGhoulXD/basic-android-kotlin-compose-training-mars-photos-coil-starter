package com.example.marsphotos.ui

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.example.marsphotos.sync.ExchangeRateWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun CurrencyScreen(viewModel: CurrencyViewModel = viewModel()) {
    val context = LocalContext.current
    val divisas by viewModel.divisasList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastSyncTime = remember { mutableStateOf(getCurrentTime()) }
    val coroutineScope = rememberCoroutineScope()
    val showHistory = remember { mutableStateOf(false) }

    // Cargar los datos del ContentProvider al iniciar
    LaunchedEffect(Unit) {
        viewModel.fetchExchangeRatesFromProvider(context)
    }

    // Configurar WorkManager para la sincronización automática
    LaunchedEffect(Unit) {
        val workRequest = PeriodicWorkRequestBuilder<ExchangeRateWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "exchange_rate_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Divisas", style = MaterialTheme.typography.headlineMedium)
        Text("Última actualización: ${lastSyncTime.value}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                coroutineScope.launch { viewModel.fetchExchangeRatesFromProvider(context) }
            }) {
                Text("Actualizar desde DB")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                coroutineScope.launch { viewModel.fetchDivisas() }
            }) {
                Text("Actualizar desde API")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showHistory.value = !showHistory.value }) {
            Text(if (showHistory.value) "Ver Tasas Actuales" else "Ver Historial")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(divisas) { divisa ->
                    if (showHistory.value) {
                        DivisaItemWithGraph(divisa)
                    } else {
                        DivisaItem(divisa)
                    }
                }
            }
        }
    }
}

@Composable
fun DivisaItem(divisa: Divisa) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Moneda: ${divisa.currency}")
        Text(text = "Tasa de cambio: ${divisa.exchangeRate}")
        Text(text = "Última actualización: ${divisa.lastUpdatedTime}")
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun DivisaItemWithGraph(divisa: Divisa) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Moneda: ${divisa.currency}")
        Text(text = "Tasa de cambio: ${divisa.exchangeRate}")
        Text(text = "Última actualización: ${divisa.lastUpdatedTime}")
        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Text("Gráfico de la divisa:")
        Spacer(modifier = Modifier.height(8.dp))

        // Convertir el historial a formato de pares (hora, tasa)
        val exchangeRateHistory = divisa.exchangeRateHistory.mapIndexed { index, rate ->
            Pair(getTimeForIndex(index), rate)
        }

        ExchangeRateGraph(exchangeRateHistory)
    }
}

@Composable
fun ExchangeRateGraph(exchangeRateHistory: List<Pair<String, Float>>) {
    if (exchangeRateHistory.isEmpty()) return

    val maxRate = exchangeRateHistory.maxOf { it.second }
    val minRate = exchangeRateHistory.minOf { it.second }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            if (exchangeRateHistory.size < 2) return@Canvas

            val path = Path()
            val points = exchangeRateHistory.mapIndexed { index, pair ->
                val x = index.toFloat() / (exchangeRateHistory.size - 1) * width
                val y = height - ((pair.second - minRate) / (maxRate - minRate) * height)
                Pair(x, y)
            }

            // Iniciar la línea en el primer punto
            path.moveTo(points.first().first, points.first().second)

            // Crear el camino de la gráfica
            points.forEach { (x, y) ->
                path.lineTo(x, y)
            }

            // Dibujar la línea
            drawPath(
                path = path,
                color = Color.Red,
                style = Stroke(width = 4f)
            )

            // Dibujar puntos en la gráfica
            points.forEach { (x, y) ->
                drawCircle(
                    color = Color.Blue,
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
}

// Función para obtener la hora en base al índice
fun getTimeForIndex(index: Int): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, -index * 10) // Restar minutos para simular tiempos pasados
    return sdf.format(calendar.time)
}


fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}
