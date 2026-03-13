package com.patatus.axioma.features.notifications.di

import com.patatus.axioma.core.di.ApiRetrofit
import com.patatus.axioma.features.notifications.data.datasources.remote.api.NotificationApiService
import com.patatus.axioma.features.notifications.data.repository.NotificationsRepositoryImpl
import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModuleBinds {
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationsRepositoryImpl
    ): NotificationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NotificationModuleProvides {
    @Provides
    @Singleton
    fun provideNotificationApiService(
        @ApiRetrofit retrofit: Retrofit
    ): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }
}