package com.example.marsphotos.di

import com.example.marsphotos.network.MarsApiService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://example.com/") // Cambia la URL base a la de tu API
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideMarsApiService(retrofit: Retrofit): MarsApiService {
        return retrofit.create(MarsApiService::class.java)
    }
}
