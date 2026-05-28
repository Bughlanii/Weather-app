package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCached(
    @PrimaryKey val cityName: String,
    val temp: Float,
    val feelsLike: Float,
    val humidity: Int,
    val windSpeed: Float,
    val weatherCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)
