package com.example.marsphotos.di

import com.example.marsphotos.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, RepositoryModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
}
