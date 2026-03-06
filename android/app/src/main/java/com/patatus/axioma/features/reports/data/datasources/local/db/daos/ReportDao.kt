package com.patatus.axioma.features.reports.data.datasources.local.db.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportEntity

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ReportEntity>)

    @Query("SELECT * FROM reports ORDER BY created_at DESC")
    fun pagingSource(): PagingSource<Int, ReportEntity>

    @Query("DELETE FROM reports")
    suspend fun clearAll()
}