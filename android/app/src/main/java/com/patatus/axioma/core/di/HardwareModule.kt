package com.patatus.axioma.core.di

import com.patatus.axioma.core.hardware.biometric.AndroidBiometricAuthManager
import com.patatus.axioma.core.hardware.biometric.BiometricAuthManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindBiometricAuthManager(
        impl: AndroidBiometricAuthManager
    ): BiometricAuthManager
}