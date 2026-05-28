package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_events")
data class LocalEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val description: String,
    val dateString: String,
    val season: String, // e.g., "Spring", "Summer", "Monsoon/Sawan", "Winter"
    val cityAssociation: String, // Major city nearest to the event (Lahore, DG Khan, Multan, Karachi, Rawalpindi)
    val latitude: Double,
    val longitude: Double
)
