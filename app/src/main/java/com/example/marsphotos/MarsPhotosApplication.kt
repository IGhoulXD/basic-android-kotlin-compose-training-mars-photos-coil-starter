package com.example.marsphotos

import android.app.Application
import com.example.marsphotos.di.AppComponent

class MarsPhotosApplication : Application() {

    // Componente Dagger para inyectar las dependencias
    val appComponent by lazy {
        AppComponent.create() // Inicializamos el componente Dagger
    }

    override fun onCreate() {
        super.onCreate()
        // Las dependencias ahora están listas para ser inyectadas
    }
}
