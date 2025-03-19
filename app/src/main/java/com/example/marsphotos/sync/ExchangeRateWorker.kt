package com.example.marsphotos.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marsphotos.database.ExchangeRate
import com.example.marsphotos.database.ExchangeRateDatabase
import com.example.marsphotos.network.RetrofitService
import retrofit2.HttpException

class ExchangeRateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            // 1️⃣ Obtener instancia de la base de datos
            val db = ExchangeRateDatabase.getDatabase(applicationContext)

            // 2️⃣ Llamar a la API de ExchangeRates
            val response = RetrofitService.api.getExchangeRates("MXN", "USD")
            Log.d("ExchangeRateWorker", "API response: $response")

            // 3️⃣ Insertar los datos de las tasas de cambio obtenidas
            val exchangeRateValue = response.rate ?: 0.0000000
            val exchangeRate = ExchangeRate("USD", "MXN", exchangeRateValue)

            // Inserta o actualiza la tasa de cambio en la base de datos
            db.exchangeRateDao().insert(exchangeRate)
            Log.d("ExchangeRateWorker", "Inserted exchange rate: $exchangeRate")

            // 4️⃣ Devolver éxito si todo fue bien
            Result.success()
        } catch (e: HttpException) {
            Log.e("ExchangeRateWorker", "HTTP error", e)
            Result.retry() // Si es un error de red, vuelve a intentar
        } catch (e: Exception) {
            Log.e("ExchangeRateWorker", "Unexpected error", e)
            Result.failure() // Otros errores, fallar
        }
    }
}
