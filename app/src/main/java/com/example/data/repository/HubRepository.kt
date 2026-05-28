package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.WeatherApiClient
import com.example.data.local.EventDao
import com.example.data.local.LocalEvent
import com.example.data.local.WeatherCached
import com.example.data.local.WeatherDao
import com.example.data.preferences.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

data class CityCoords(val name: String, val lat: Double, val lon: Double)

class HubRepository(
    private val weatherDao: WeatherDao,
    private val eventDao: EventDao,
    private val preferenceManager: PreferenceManager
) {
    companion object {
        const val TAG = "HubRepository"

        val CITIES = listOf(
            CityCoords("Lahore", 31.5497, 74.3436),
            CityCoords("Rawalpindi", 33.6007, 73.0679),
            CityCoords("Karachi", 24.8607, 67.0011),
            CityCoords("Multan", 30.1575, 71.5249),
            CityCoords("Dera Ghazi Khan", 30.0561, 70.6348)
        )

        fun getCoordsForCity(cityName: String): CityCoords {
            return CITIES.firstOrNull { it.name.equals(cityName, ignoreCase = true) }
                ?: CityCoords(cityName, 31.5497, 74.3436) // Fallback to Lahore
        }
    }

    // Observing cached weather
    fun getWeatherForCityFlow(cityName: String): Flow<WeatherCached?> =
        weatherDao.getWeatherByCity(cityName)

    val allCachedWeather: Flow<List<WeatherCached>> =
        weatherDao.getAllCachedWeather()

    // Observing events
    val allEvents: Flow<List<LocalEvent>> =
        eventDao.getAllEvents()

    fun getEventsByCity(cityName: String): Flow<List<LocalEvent>> =
        eventDao.getEventsByCity(cityName)

    // User preferences delegator
    val primaryCity: Flow<String> = preferenceManager.primaryCity
    val tempUnit: Flow<String> = preferenceManager.tempUnit
    val notificationsEnabled: Flow<Boolean> = preferenceManager.notificationsEnabled
    val widgetInterval: Flow<String> = preferenceManager.widgetInterval
    val cachedAiInsight: Flow<String> = preferenceManager.cachedAiInsight

    suspend fun updatePrimaryCity(cityName: String) {
        preferenceManager.setPrimaryCity(cityName)
    }

    suspend fun updateTempUnit(unit: String) {
        preferenceManager.setTempUnit(unit)
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        preferenceManager.setNotificationsEnabled(enabled)
    }

    suspend fun updateWidgetInterval(interval: String) {
        preferenceManager.setWidgetInterval(interval)
    }

    // Refresh weather for a single city from API and save to Cache
    suspend fun refreshWeatherForCity(cityName: String): Boolean {
        return try {
            val coords = getCoordsForCity(cityName)
            val response = WeatherApiClient.service.getCurrentWeather(coords.lat, coords.lon)
            val current = response.current
            if (current != null) {
                val cached = WeatherCached(
                    cityName = cityName,
                    temp = current.temperature,
                    feelsLike = current.apparentTemperature,
                    humidity = current.humidity,
                    windSpeed = current.windSpeed,
                    weatherCode = current.weatherCode
                )
                weatherDao.insertWeather(cached)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing weather for $cityName", e)
            false
        }
    }

    // Refresh weather for all 5 major comparison cities
    suspend fun refreshAllCities(): Boolean {
        var success = true
        for (city in CITIES) {
            val res = refreshWeatherForCity(city.name)
            if (!res) success = false
        }
        return success
    }

    // Call Gemini to scan the local season + selected city weather + cultural context
    suspend fun fetchGeminiInsight(cityName: String, currentTemp: Float, weatherCode: String, desiMonth: String): String {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return "Gemini API key is unconfigured. Please insert your valid GEMINI_API_KEY in the AI Studio Secrets panel. (Using local offline smart engine as fallback)."
            }

            val prompt = """
                You are the AI engine of 'Smart Weather & Local Calendar Hub', operating in the Punjab/Saraiki regions of Pakistan.
                Current context:
                - Selected City: $cityName
                - Weather: $currentTemp°C, Description: $weatherCode
                - Punjabi Desi Solar Month: $desiMonth
                
                Please generate a short, highly engaging 2-3 sentence regional summary providing:
                1. A culturally warm greeting (in Punjabi/Saraiki style combined with English).
                2. A weather-based smart suggestion (e.g., drink more water, carry an umbrella, or avoid peak afternoon sun).
                3. A mention of an upcoming cultural or religious mela relevant to this city or season.
                Avoid any markdown formatting other than text. Keep it brief, inviting, and practical.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )

            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val insight = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to extract response from Gemini."
            preferenceManager.cacheAiInsight(insight)
            insight
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Gemini insight", e)
            "Offline mode: Unable to connect to Gemini API. ${e.localizedMessage ?: "Please check internet connections."}"
        }
    }
}
