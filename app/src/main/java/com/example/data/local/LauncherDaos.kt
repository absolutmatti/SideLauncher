package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppMetadata
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist")
    fun getAllWatchlistFlow(): Flow<List<WatchlistItem>>

    @Query("SELECT * FROM watchlist WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WatchlistItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WatchlistItem>)

    @Update
    suspend fun update(item: WatchlistItem)

    @Delete
    suspend fun delete(item: WatchlistItem)

    @Query("DELETE FROM watchlist WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM watchlist")
    suspend fun deleteAll()
}

@Dao
interface AppMetadataDao {
    @Query("SELECT * FROM app_metadata")
    fun getAllMetadataFlow(): Flow<List<AppMetadata>>

    @Query("SELECT * FROM app_metadata WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): AppMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: AppMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AppMetadata>)

    @Update
    suspend fun update(metadata: AppMetadata)

    @Query("DELETE FROM app_metadata WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
