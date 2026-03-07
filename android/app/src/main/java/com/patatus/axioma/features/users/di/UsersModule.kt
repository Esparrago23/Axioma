package com.patatus.axioma.features.users.di

import com.patatus.axioma.core.di.ApiRetrofit
import com.patatus.axioma.features.users.data.datasources.remote.api.UsersApiService
import com.patatus.axioma.features.users.data.repositories.UsersRepositoryImpl
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UsersModuleBinds {
    @Binds
    @Singleton
    abstract fun bindUsersRepository(
        impl: UsersRepositoryImpl
    ): UsersRepository
}

@Module
@InstallIn(SingletonComponent::class)
object UsersModuleProvides {
    @Provides
    @Singleton
    fun provideUsersApiService(
        @ApiRetrofit retrofit: Retrofit
    ): UsersApiService {
        return retrofit.create(UsersApiService::class.java)
    }
}
