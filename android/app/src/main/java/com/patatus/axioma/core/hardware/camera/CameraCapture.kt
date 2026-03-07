package com.patatus.axioma.core.hardware.camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class CameraCaptureLauncher(
    val launch: () -> Unit
)

@Composable
fun rememberCameraCaptureLauncher(
    onImageCaptured: (Uri) -> Unit
): CameraCaptureLauncher {
    val context = LocalContext.current
    val pendingUriState = remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingUriState.value?.let(onImageCaptured)
        }
    }

    return remember(takePictureLauncher, context) {
        CameraCaptureLauncher(
            launch = {
                val uri = CameraFileFactory.createTempImageUri(context)
                pendingUriState.value = uri
                takePictureLauncher.launch(uri)
            }
        )
    }
}
