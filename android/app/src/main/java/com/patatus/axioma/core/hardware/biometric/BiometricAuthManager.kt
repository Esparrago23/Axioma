package com.patatus.axioma.core.hardware.biometric

import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability {
    AVAILABLE,
    NONE_ENROLLED,
    NO_HARDWARE,
    UNAVAILABLE,
}


interface BiometricAuthManager {
    fun checkAvailability(activity: FragmentActivity): BiometricAvailability

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}