package com.example.marsphotos.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    // Esta es la nueva ruta que toma como parámetros el par de divisas (ejemplo: MXN/COP)
    @GET("pair/{base_currency}/{target_currency}")
    suspend fun getExchangeRates(@Path("base_currency") baseCurrency: String, @Path("target_currency") targetCurrency: String): ExchangeRateResponse

}

data class ExchangeRateResponse(val rate: Double)

object RetrofitService {
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/32c3c6a6cb623d4b8bb846dd/"

    val api: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}
