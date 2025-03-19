package com.example.marsphotos

import android.app.Application
import android.content.Context
import android.content.Intent

class MarsPhotosApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Usamos un SharedPreference para guardar el estado
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("should_open_main_activity", true)
        editor.apply()
    }
}
