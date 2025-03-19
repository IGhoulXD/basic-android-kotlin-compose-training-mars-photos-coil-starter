package com.example.marsphotos.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rate_history")
data class ExchangeRateHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "currency") val currency: String,
    @ColumnInfo(name = "rate") val rate: Double,
    @ColumnInfo(name = "timestamp") val timestamp: String // Hora en formato HH:mm:ss
)
