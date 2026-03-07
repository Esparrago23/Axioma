package com.patatus.axioma.core.hardware.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

data class CameraPermissionRequester(
    val hasPermission: Boolean,
    val shouldShowRationale: Boolean,
    val requestPermission: () -> Unit
)

@Composable
fun rememberCameraPermissionRequester(): CameraPermissionRequester {
    val context = LocalContext.current
    val activity = context.findActivity()

    var hasPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val showRationale = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
    } ?: false

    return CameraPermissionRequester(
        hasPermission = hasPermission,
        shouldShowRationale = !hasPermission && showRationale,
        requestPermission = { launcher.launch(Manifest.permission.CAMERA) }
    )
}

private fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
