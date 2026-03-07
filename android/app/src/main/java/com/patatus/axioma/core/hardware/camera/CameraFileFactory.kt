package com.patatus.axioma.core.hardware.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraFileFactory {
    private const val FILE_PREFIX = "axioma_profile_"
    private const val FILE_SUFFIX = ".jpg"

    fun createTempImageFile(context: Context): File {
        return File.createTempFile(FILE_PREFIX, FILE_SUFFIX, context.cacheDir)
    }

    fun createTempImageUri(context: Context): Uri {
        val file = createTempImageFile(context)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
