package com.example.marsphotos.di

import com.example.marsphotos.data.MarsPhotosRepository
import com.example.marsphotos.data.NetworkMarsPhotosRepository
import com.example.marsphotos.network.MarsApiService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideMarsPhotosRepository(marsApiService: MarsApiService): MarsPhotosRepository {
        return NetworkMarsPhotosRepository(marsApiService)
    }
}
