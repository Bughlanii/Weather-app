package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import com.example.R
import com.example.data.local.HubDatabase
import com.example.data.local.WeatherCached
import com.example.data.repository.HubRepository
import com.example.ui.utils.CalendarUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "WeatherWidgetProvider"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Run background refresh and binder updates using standard background scope
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val db = HubDatabase.getDatabase(context)
                val weatherDao = db.weatherDao()

                // Read cached weather metrics
                val cachedList = weatherDao.getAllCachedWeather().first()

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.weather_widget_layout)

                    // Bind update timestamp
                    val timeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    views.setTextViewText(R.id.widget_update_time, "Updated at $timeString")

                    // Map all 5 cities to their respective view layout IDs
                    bindCityToWidget(views, "Lahore", cachedList, R.id.widget_city_1_temp, R.id.widget_city_1_icon)
                    bindCityToWidget(views, "Rawalpindi", cachedList, R.id.widget_city_2_temp, R.id.widget_city_2_icon)
                    bindCityToWidget(views, "Karachi", cachedList, R.id.widget_city_3_temp, R.id.widget_city_3_icon)
                    bindCityToWidget(views, "Multan", cachedList, R.id.widget_city_4_temp, R.id.widget_city_4_icon)
                    bindCityToWidget(views, "Dera Ghazi Khan", cachedList, R.id.widget_city_5_temp, R.id.widget_city_5_icon)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rendering comparative widget", e)
            }
        }
    }

    private fun bindCityToWidget(
        views: RemoteViews,
        cityName: String,
        cachedList: List<WeatherCached>,
        tempViewId: Int,
        iconViewId: Int
    ) {
        val weather = cachedList.firstOrNull { it.cityName.equals(cityName, ignoreCase = true) }
        if (weather != null) {
            val emojiPair = CalendarUtils.getWeatherDescriptionAndIcon(weather.weatherCode)
            views.setTextViewText(tempViewId, "${weather.temp.toInt()}°C")
            views.setTextViewText(iconViewId, emojiPair.second)
        } else {
            views.setTextViewText(tempViewId, "--°C")
            views.setTextViewText(iconViewId, "🌡️")
        }
    }
}
