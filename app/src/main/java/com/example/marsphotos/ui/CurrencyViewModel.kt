package com.example.marsphotos.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.data.ExchangeRateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

// Modelo de las divisas
data class ExchangeRate(val currency: String, val rate: Double)

data class Divisa(
    val currency: String,
    val exchangeRate: Float,
    val lastUpdatedTime: String,
    val exchangeRateHistory: List<Float>
)

// API interface para Retrofit
interface DivisasApi {
    @GET("v4/latest/MXN")
    suspend fun getDivisas(): DivisasResponse
}

// Respuesta de la API
data class DivisasResponse(val rates: Map<String, Double>)

class CurrencyViewModel : ViewModel() {

    private val _exchangeRates = MutableStateFlow<List<ExchangeRate>>(emptyList())
    val exchangeRates: StateFlow<List<ExchangeRate>> = _exchangeRates

    val divisasList = MutableStateFlow<List<Divisa>>(emptyList())
    val isLoading = MutableStateFlow(false)

    // Configuración de Retrofit para obtener las divisas
    private val divisasApi = Retrofit.Builder()
        .baseUrl("https://api.exchangerate-api.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DivisasApi::class.java)

    init {
        fetchDivisas()
    }

    // Función que obtiene las divisas y las filtra
    fun fetchDivisas() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = divisasApi.getDivisas()
                val monedasFiltradas = listOf("USD", "CAD", "EUR", "COP")
                val divisas = monedasFiltradas.map { moneda ->
                    val exchangeRate = response.rates[moneda]?.toFloat() ?: 0.0f
                    Divisa(
                        currency = moneda,
                        exchangeRate = exchangeRate,
                        lastUpdatedTime = getCurrentTime(),
                        exchangeRateHistory = emptyList() // Se actualizará después con el ContentProvider
                    )
                }
                divisasList.value = divisas
                Log.d("CurrencyViewModel", "Divisas actualizadas: $divisas")
            } catch (e: Exception) {
                Log.e("CurrencyViewModel", "Error fetching divisas", e)
            }
            isLoading.value = false
        }
    }

    // Obtener la fecha actual
    private fun getCurrentTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun fetchExchangeRatesFromProvider(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val uri = ExchangeRateProvider.CONTENT_URI
                val projection = arrayOf("currency", "rate", "timestamp")
                val cursor = context.contentResolver.query(uri, projection, null, null, "timestamp DESC")

                cursor?.use {
                    val historicalData = mutableMapOf<String, MutableList<Pair<String, Float>>>()
                    val divisas = mutableListOf<Divisa>()

                    val currencyIndex = it.getColumnIndexOrThrow("currency")
                    val rateIndex = it.getColumnIndexOrThrow("rate")
                    val timestampIndex = it.getColumnIndexOrThrow("timestamp")

                    while (it.moveToNext()) {
                        val currency = it.getString(currencyIndex)
                        val rate = it.getDouble(rateIndex).toFloat()
                        val timestamp = it.getString(timestampIndex)

                        historicalData.getOrPut(currency) { mutableListOf() }.add(timestamp to rate)
                    }

                    // Construir la lista final de divisas con historial real
                    for ((currency, rates) in historicalData) {
                        divisas.add(
                            Divisa(
                                currency = currency,
                                exchangeRate = rates.first().second, // Última tasa disponible
                                lastUpdatedTime = rates.first().first, // Última fecha disponible
                                exchangeRateHistory = fetchExchangeRateHistoryFromProvider(context, currency) // Obtener historial real
                            )
                        )
                    }

                    divisasList.value = divisas
                    Log.d("CurrencyViewModel", "Datos obtenidos del ContentProvider: $divisas")
                }
            } catch (e: Exception) {
                Log.e("CurrencyViewModel", "Error al obtener datos del ContentProvider", e)
            }
            isLoading.value = false
        }
    }

    private fun fetchExchangeRateHistoryFromProvider(context: Context, currency: String): List<Float> {
        val uri = ExchangeRateProvider.CONTENT_URI
        val projection = arrayOf("rate")
        val selection = "currency = ?"
        val selectionArgs = arrayOf(currency)
        val sortOrder = "timestamp DESC LIMIT 48"
        val history = mutableListOf<Float>()

        try {
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            cursor?.use {
                val rateIndex = it.getColumnIndexOrThrow("rate")
                while (it.moveToNext()) {
                    history.add(it.getDouble(rateIndex).toFloat())
                }
            }
        } catch (e: Exception) {
            Log.e("CurrencyViewModel", "Error al obtener historial de tasas para $currency", e)
        }
        return history
    }
}
