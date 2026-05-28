package com.example.ui.utils

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.chrono.IsoChronology
import java.time.chrono.HijrahChronology
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DesiDate(
    val day: Int,
    val monthName: String,
    val year: Int,
    val description: String
)

object CalendarUtils {

    private val HIJRI_MONTHS = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijgah"
    )

    private val DESI_MONTHS = listOf(
        "Chet", "Vaisakh", "Jeth", "Harh", "Sawan", "Bhadon",
        "Assu", "Katten", "Magghar", "Poh", "Mangh", "Phaggan"
    )

    // Formats a Hijrah date natively using Android APIs
    fun formatHijriDate(date: LocalDate): String {
        return try {
            val hijriDate = HijrahDate.from(date)
            val monthIdx = hijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) - 1
            val monthName = HIJRI_MONTHS.getOrElse(monthIdx) { "Month $monthIdx" }
            val day = hijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val year = hijriDate.get(java.time.temporal.ChronoField.YEAR)
            "$day $monthName $year AH"
        } catch (e: Exception) {
            "Error Calculating Hijri Date"
        }
    }

    // Convert Gregorian Date to Punjabi/Saraiki Desi (Bikrami Solar) Calendar
    fun getPunjabiDesiDate(date: LocalDate): DesiDate {
        val year = date.year
        val leap = date.isLeapYear
        val dayOfYear = date.dayOfYear

        val bikramiYear = year + 57

        // Desi month transitions based on Gregorian dates
        val month: String
        val day: Int

        when {
            // January 1 - January 13 is Poh (starts Dec 16)
            date.monthValue == 1 && date.dayOfMonth <= 13 -> {
                month = "Poh"
                day = date.dayOfMonth + 16 // Dec is 16 days of Poh
            }
            // January 14 - February 12 is Mangh
            (date.monthValue == 1 && date.dayOfMonth >= 14) || (date.monthValue == 2 && date.dayOfMonth <= 12) -> {
                month = "Mangh"
                day = if (date.monthValue == 1) {
                    date.dayOfMonth - 13
                } else {
                    date.dayOfMonth + 18 // 18 days of Jan
                }
            }
            // February 13 - March 13 is Phaggan
            (date.monthValue == 2 && date.dayOfMonth >= 13) || (date.monthValue == 3 && date.dayOfMonth <= 13) -> {
                month = "Phaggan"
                day = if (date.monthValue == 2) {
                    date.dayOfMonth - 12
                } else {
                    val febDays = if (leap) 17 else 16 // compensate for leap day
                    date.dayOfMonth + febDays
                }
            }
            // March 14 - April 13 is Chet (Desi solar New Year approx April 14, but season starts Chet)
            (date.monthValue == 3 && date.dayOfMonth >= 14) || (date.monthValue == 4 && date.dayOfMonth <= 13) -> {
                month = "Chet"
                day = if (date.monthValue == 3) {
                    date.dayOfMonth - 13
                } else {
                    date.dayOfMonth + 18
                }
            }
            // April 14 - May 14 is Vaisakh
            (date.monthValue == 4 && date.dayOfMonth >= 14) || (date.monthValue == 5 && date.dayOfMonth <= 14) -> {
                month = "Vaisakh"
                day = if (date.monthValue == 4) {
                    date.dayOfMonth - 13
                } else {
                    date.dayOfMonth + 17
                }
            }
            // May 15 - June 15 is Jeth
            (date.monthValue == 5 && date.dayOfMonth >= 15) || (date.monthValue == 6 && date.dayOfMonth <= 15) -> {
                month = "Jeth"
                day = if (date.monthValue == 5) {
                    date.dayOfMonth - 14
                } else {
                    date.dayOfMonth + 17
                }
            }
            // June 16 - July 16 is Harh
            (date.monthValue == 6 && date.dayOfMonth >= 16) || (date.monthValue == 7 && date.dayOfMonth <= 16) -> {
                month = "Harh"
                day = if (date.monthValue == 6) {
                    date.dayOfMonth - 15
                } else {
                    date.dayOfMonth + 15
                }
            }
            // July 17 - August 16 is Sawan
            (date.monthValue == 7 && date.dayOfMonth >= 17) || (date.monthValue == 8 && date.dayOfMonth <= 16) -> {
                month = "Sawan"
                day = if (date.monthValue == 7) {
                    date.dayOfMonth - 16
                } else {
                    date.dayOfMonth + 15
                }
            }
            // August 17 - September 16 is Bhadon
            (date.monthValue == 8 && date.dayOfMonth >= 17) || (date.monthValue == 9 && date.dayOfMonth <= 16) -> {
                month = "Bhadon"
                day = if (date.monthValue == 8) {
                    date.dayOfMonth - 16
                } else {
                    date.dayOfMonth + 15
                }
            }
            // September 17 - October 17 is Assu
            (date.monthValue == 9 && date.dayOfMonth >= 17) || (date.monthValue == 10 && date.dayOfMonth <= 17) -> {
                month = "Assu"
                day = if (date.monthValue == 9) {
                    date.dayOfMonth - 16
                } else {
                    date.dayOfMonth + 14
                }
            }
            // October 18 - November 16 is Katten
            (date.monthValue == 10 && date.dayOfMonth >= 18) || (date.monthValue == 11 && date.dayOfMonth <= 16) -> {
                month = "Katten"
                day = if (date.monthValue == 10) {
                    date.dayOfMonth - 17
                } else {
                    date.dayOfMonth + 14
                }
            }
            // November 17 - December 15 is Magghar
            (date.monthValue == 11 && date.dayOfMonth >= 17) || (date.monthValue == 12 && date.dayOfMonth <= 15) -> {
                month = "Magghar"
                day = if (date.monthValue == 11) {
                    date.dayOfMonth - 16
                } else {
                    date.dayOfMonth + 14
                }
            }
            // December 16 - December 31 is Poh
            else -> {
                month = "Poh"
                day = date.dayOfMonth - 15
            }
        }

        val localSeason = when (month) {
            "Chet", "Vaisakh" -> "Bahar (Spring)"
            "Jeth", "Harh" -> "Unha (Summer-Dry)"
            "Sawan", "Bhadon" -> "Barsaat (Monsoon/Sawan)"
            "Assu", "Katten" -> "Patjhar (Autumn)"
            "Magghar", "Poh", "Mangh", "Phaggan" -> "Siyyal (Winter)"
            else -> "Regular Season"
        }

        return DesiDate(
            day = day,
            monthName = month,
            year = bikramiYear,
            description = "$day $month $bikramiYear Bikrami ($localSeason)"
        )
    }

    // Get WMO code display details as human readable text + icon reference
    fun getWeatherDescriptionAndIcon(weatherCode: Int): Pair<String, String> {
        return when (weatherCode) {
            0 -> "Clear Sky" to "☀️"
            1, 2, 3 -> "Partly Cloudy" to "⛅"
            45, 48 -> "Foggy Weather" to "🌫️"
            51, 53, 55 -> "Light Drizzle" to "🌦️"
            61, 63, 65 -> "Continuous Rain" to "🌧️"
            66, 67 -> "Freezing Rain" to "🌨️"
            71, 73, 75 -> "Snowfall" to "❄️"
            77 -> "Snow Grains" to "❄️"
            80, 81, 82 -> "Rain Showers" to "🌧️"
            85, 86 -> "Snow Showers" to "🌨️"
            95 -> "Thunderstorm" to "⛈️"
            96, 99 -> "Heavy Thunderstorm with Hail" to "⛈️"
            else -> "Mild Weather" to "🌡️"
        }
    }
}
