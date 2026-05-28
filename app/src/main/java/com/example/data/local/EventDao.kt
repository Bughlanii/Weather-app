package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM local_events ORDER BY id ASC")
    fun getAllEvents(): Flow<List<LocalEvent>>

    @Query("SELECT * FROM local_events WHERE cityAssociation = :city")
    fun getEventsByCity(city: String): Flow<List<LocalEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<LocalEvent>)

    @Query("DELETE FROM local_events")
    suspend fun clearEvents()
}
