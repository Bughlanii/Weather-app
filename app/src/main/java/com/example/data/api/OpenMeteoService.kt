package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: CurrentWeatherResponse?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherResponse(
    @Json(name = "time") val time: String,
    @Json(name = "temperature_2m") val temperature: Float,
    @Json(name = "relative_humidity_2m") val humidity: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Float,
    @Json(name = "wind_speed_10m") val windSpeed: Float,
    @Json(name = "weather_code") val weatherCode: Int
)

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentParams: String = "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code"
    ): OpenMeteoResponse
}

object WeatherApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: OpenMeteoService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenMeteoService::class.java)
    }
}
