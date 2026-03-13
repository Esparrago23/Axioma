package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class UploadReportPhotoUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(localUri: String): Result<String> {
        return repository.uploadReportPhoto(localUri)
    }
}
