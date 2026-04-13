package com.patatus.axioma.features.comments.di

import com.patatus.axioma.core.di.ApiRetrofit
import com.patatus.axioma.features.comments.data.remote.api.CommentsApiService
import com.patatus.axioma.features.comments.data.repositories.CommentsRepositoryImpl
import com.patatus.axioma.features.comments.domain.repositories.CommentsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommentsModuleBinds {
    @Binds
    @Singleton
    abstract fun bindCommentsRepository(impl: CommentsRepositoryImpl): CommentsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CommentsModuleProvides {
    @Provides
    @Singleton
    fun provideCommentsApiService(@ApiRetrofit retrofit: Retrofit): CommentsApiService =
        retrofit.create(CommentsApiService::class.java)
}
