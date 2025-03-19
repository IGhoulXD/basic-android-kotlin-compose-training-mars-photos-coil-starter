package com.example.marsphotos.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExchangeRateHistoryDao {
    @Insert
    suspend fun insertHistory(rateHistory: ExchangeRateHistory)

    @Query("SELECT * FROM exchange_rate_history ORDER BY timestamp DESC")
    fun getAllHistory(): LiveData<List<ExchangeRateHistory>>
}
