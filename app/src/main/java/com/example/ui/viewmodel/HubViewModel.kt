package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HubDatabase
import com.example.data.local.LocalEvent
import com.example.data.local.WeatherCached
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.HubRepository
import com.example.ui.utils.CalendarUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class HubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HubRepository

    init {
        val database = HubDatabase.getDatabase(application)
        val preferenceManager = PreferenceManager(application)
        repository = HubRepository(
            weatherDao = database.weatherDao(),
            eventDao = database.eventDao(),
            preferenceManager = preferenceManager
        )

        // Pre-fetch weather on startup in background
        viewModelScope.launch {
            repository.refreshAllCities()
        }
    }

    // Exposed flows from database preferences data source
    val primaryCity = repository.primaryCity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferenceManager.DEFAULT_CITY
    )

    val tempUnit = repository.tempUnit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferenceManager.UNIT_CELSIUS
    )

    val notificationsEnabled = repository.notificationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val widgetInterval = repository.widgetInterval.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "30 Minutes"
    )

    val cachedAiInsight = repository.cachedAiInsight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Click 'Scan with Gemini AI' to load predictive local insights."
    )

    // Current primary city details
    val primaryCityWeather: Flow<WeatherCached?> = primaryCity.flatMapLatest { city ->
        repository.getWeatherForCityFlow(city)
    }

    // 5 cities comparison weather list
    val allCachedWeather = repository.allCachedWeather.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter events based on active primary city
    val primaryCityEvents: Flow<List<LocalEvent>> = primaryCity.flatMapLatest { city ->
        repository.getEventsByCity(city)
    }

    // All local cultural events
    val allEvents = repository.allEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI State variables
    val activeTab = MutableStateFlow("home") // "home", "comparison", "events", "settings"
    val isRefreshing = MutableStateFlow(false)
    val geminiLoading = MutableStateFlow(false)
    val snackbarMessage = MutableStateFlow<String?>(null)

    // Switch screen tab selector
    fun selectTab(tab: String) {
        activeTab.value = tab
    }

    // Trigger full synchronized refresh
    fun refreshAllWeatherData() {
        viewModelScope.launch {
            isRefreshing.value = true
            val success = repository.refreshAllCities()
            isRefreshing.value = false
            if (success) {
                snackbarMessage.value = "Weather refreshed for all Punjab locations."
            } else {
                snackbarMessage.value = "Offline mode: Displaying cached weather."
            }
        }
    }

    // Trigger explicit single-city refresh
    fun refreshPrimaryCityOnly() {
        viewModelScope.launch {
            isRefreshing.value = true
            val success = repository.refreshWeatherForCity(primaryCity.value)
            isRefreshing.value = false
            if (success) {
                snackbarMessage.value = "Refreshed weather for ${primaryCity.value}."
            } else {
                snackbarMessage.value = "Unable to connect. Loaded cached values."
            }
        }
    }

    // Save preferences
    fun setPrimaryCity(city: String) {
        viewModelScope.launch {
            repository.updatePrimaryCity(city)
            refreshPrimaryCityOnly()
        }
    }

    fun setTempUnit(unit: String) {
        viewModelScope.launch {
            repository.updateTempUnit(unit)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateNotificationsEnabled(enabled)
        }
    }

    fun setWidgetInterval(interval: String) {
        viewModelScope.launch {
            repository.updateWidgetInterval(interval)
        }
    }

    // Fetch live Gemini suggestion for selected context
    fun requestGeminiInsight() {
        viewModelScope.launch {
            geminiLoading.value = true
            val weather = primaryCityWeather.first()
            val temp = weather?.temp ?: 28.0f
            val codeDesc = weather?.let { CalendarUtils.getWeatherDescriptionAndIcon(it.weatherCode).first } ?: "Mild"
            val desi = CalendarUtils.getPunjabiDesiDate(LocalDate.now()).monthName

            val result = repository.fetchGeminiInsight(
                cityName = primaryCity.value,
                currentTemp = temp,
                weatherCode = codeDesc,
                desiMonth = desi
            )
            geminiLoading.value = false
            snackbarMessage.value = "Gemini Insight Updated Successfully!"
        }
    }

    // Clear alert snackbar trigger
    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    // Local smart offline recommendation engine
    fun getLocalSmartRecommendation(weather: WeatherCached?, desiMonth: String): List<String> {
        val list = mutableListOf<String>()
        if (weather == null) {
            list.add("Stay hydrated! Always carry local Lassi or cold water in the summer heat.")
            return list
        }

        val temp = weather.temp
        val code = weather.weatherCode

        // Temperature-based heuristics
        when {
            temp > 35f -> {
                list.add("☀️ Summer Heat Peak: Drink high amounts of fluids like Nimbu Paani or cooling Lassi.")
                list.add("👔 Clothing Advice: Wear light cotton Kameez to minimize heat strokes.")
            }
            temp < 15f -> {
                list.add("❄️ Mild Winter Cold: Keep warm with a Shawl or jacket (Desi Loie) during chilly evenings.")
                list.add("🍵 Local Delicacy Advice: Savor hot Kashmiri Chai or Halwa in tonight's weather.")
            }
            else -> {
                list.add("🍃 Mild Weather: Excellent time for outdoor tasks and visiting local markets or shrines.")
            }
        }

        // Weather Code-based alerts
        when (code) {
            in 61..82 -> {
                list.add("🌧️ Rain / Monsoon alert: Keep an umbrella near, secure grain stores, and avoid electric grid lines.")
            }
            in 95..99 -> {
                list.add("⛈️ Severe Thunderstorms: Remain indoors, unplug sensitive machinery, and monitor localized rain.")
            }
            in 45..48 -> {
                list.add("🌫️ Chilly Fog (Smog in Winters): Drive with fog lamps active and wear face masks in high density areas.")
            }
        }

        // Desi month-specific culturally adjusted suggestions:
        when (desiMonth) {
            "Chet", "Vaisakh" -> {
                list.add("🌾 Harvest season is here for wheat grains (Kanak). Support local farmers in the neighborhood.")
            }
            "Sawan", "Bhadon" -> {
                list.add("☔ Barsaat season (Monsoon): Enjoy fresh pakoras, and watch for beautiful rain showers in South Punjab.")
            }
            "Jeth", "Harh" -> {
                list.add("🌞 Extreme dry summer winds (Loo): Shield your head and skin when traveling in DG Khan or Multan.")
            }
        }

        return list
    }
}
