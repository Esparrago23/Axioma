package com.patatus.axioma.features.notifications.data.datasources.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationRemoteKeysEntity

@Dao
interface NotificationRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<NotificationRemoteKeysEntity>)

    @Query("SELECT * FROM notification_remote_keys WHERE notificationId = :notificationId")
    suspend fun remoteKeysNotificationId(notificationId: Int): NotificationRemoteKeysEntity?

    @Query("DELETE FROM notification_remote_keys")
    suspend fun clearRemoteKeys()
}