package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName LIMIT 1")
    fun getWeatherByCity(cityName: String): Flow<WeatherCached?>

    @Query("SELECT * FROM weather_cache")
    fun getAllCachedWeather(): Flow<List<WeatherCached>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCached)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWeather(weathers: List<WeatherCached>)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}
