package com.example.marsphotos.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey val currency: String,
    val currencyCode: String,
    val exchangeRateValue: Double
    )
