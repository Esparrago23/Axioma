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
    fun pagingSourceRecent(): PagingSource<Int, ReportEntity>

    @Query("SELECT * FROM reports WHERE created_at >= :sinceIso ORDER BY credibility_score DESC, created_at DESC")
    fun pagingSourceRelevant(sinceIso: String): PagingSource<Int, ReportEntity>

    @Query("DELETE FROM reports")
    suspend fun clearAll()
}