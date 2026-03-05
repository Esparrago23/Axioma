package com.patatus.axioma.features.auth.di

import com.patatus.axioma.core.di.ApiRetrofit
import com.patatus.axioma.features.auth.data.datasources.remote.api.AuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModuleProvides {

    @Provides
    @Singleton
    fun provideAuthApiService(
        @ApiRetrofit retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}
/*
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(appContainer.authRepository)
    }

    private fun provideRegisterUseCase(): RegisterUseCase {
        return RegisterUseCase(appContainer.authRepository)
    }


    fun provideLoginViewModelFactory(): LoginViewModelFactory {
        return LoginViewModelFactory(provideLoginUseCase())
    }

    fun provideRegisterViewModelFactory(): RegisterViewModelFactory {
        return RegisterViewModelFactory(provideRegisterUseCase())
    }
}
*/
