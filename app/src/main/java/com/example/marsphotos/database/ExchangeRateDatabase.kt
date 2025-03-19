package com.example.marsphotos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ExchangeRate::class], version = 2)
abstract class ExchangeRateDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        @Volatile
        private var INSTANCE: ExchangeRateDatabase? = null

        // Migración de la versión 1 a la versión 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Ejemplo: Si quieres agregar una columna nueva, haces lo siguiente
                database.execSQL("ALTER TABLE exchange_rate ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Método para obtener la instancia de la base de datos
        fun getDatabase(context: Context): ExchangeRateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExchangeRateDatabase::class.java,
                    "exchange_rate_db"
                )
                    // Añadimos la migración personalizada
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
