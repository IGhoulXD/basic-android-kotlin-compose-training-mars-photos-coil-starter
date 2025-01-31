package com.example.marsphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.marsphotos.data.MarsPhotosRepository
import com.example.marsphotos.ui.MarsPhotosApp
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var marsPhotosRepository: MarsPhotosRepository // Inyectamos el repositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inyectamos las dependencias usando Dagger
        (application as MarsPhotosApplication).appComponent.inject(this)

        setContent {
            MarsPhotosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MarsPhotosApp()
                }
            }
        }
    }
}
