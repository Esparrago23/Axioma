package com.patatus.axioma.features.reports.di

import com.patatus.axioma.core.di.ApiRetrofit
import com.patatus.axioma.features.reports.data.datasources.remote.api.ReportsApiService
import com.patatus.axioma.features.reports.data.repositories.ReportsRepositoryImpl
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportsModuleBinds {
    @Binds
    @Singleton
    abstract fun bindReportsRepository(
        impl: ReportsRepositoryImpl
    ): ReportsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ReportsModuleProvides {
    @Provides
    @Singleton
    fun provideReportsApiService(
        @ApiRetrofit retrofit: Retrofit
    ): ReportsApiService {
        return retrofit.create(ReportsApiService::class.java)
    }
}