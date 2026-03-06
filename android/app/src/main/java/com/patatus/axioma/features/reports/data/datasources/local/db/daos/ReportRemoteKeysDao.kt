package com.patatus.axioma.features.reports.data.datasources.local.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportRemoteKeysEntity

@Dao
interface ReportRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<ReportRemoteKeysEntity>)

    @Query("SELECT * FROM report_remote_keys WHERE reportId = :id")
    suspend fun remoteKeysReportId(id: Int): ReportRemoteKeysEntity?

    @Query("DELETE FROM report_remote_keys")
    suspend fun clearRemoteKeys()
}