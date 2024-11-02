package com.example.weather.utils

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.util.Date
import java.util.Locale

object DateAndTimeUtils {
    private const val ISO8601_STANDARD_REGEX_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$"
    private const val TIME_PATTERN = "HH:mm"
    private const val DATE_PATTERN = "yyyy-MM-dd"

    /**
     * @return the first value in pair is a date, the second one is a time;
     */
    fun splitDateAndTime(dateAndTime: String): Pair<String, String> {
        if (dateAndTime.matches(ISO8601_STANDARD_REGEX_PATTERN.toRegex())) {
            val (date, time) = dateAndTime.split("T")
            return Pair(date, time)
        } else {
            throw IllegalArgumentException("Expected date and time format YYYY-MM-DDThh:mm, but received: $dateAndTime")
        }
    }

    fun compareDates(firstDate: String, secondDate: String): Int {
        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())

        return try {
            val date1: Date = dateFormat.parse(firstDate)
                ?: throw IllegalArgumentException("Date parsing failed for $firstDate")
            val date2: Date = dateFormat.parse(secondDate)
                ?: throw IllegalArgumentException("Date parsing failed for $secondDate")

            when {
                date1.before(date2) -> -1 // firstDate is earlier
                date1.after(date2) -> 1   // firstDate is later
                else -> 0                 // dates are equal
            }
        } catch (e: Exception) {
            println("Error comparing dates: ${e.message}")
            -2
        }
    }

    fun compareHours(firstTime: String, secondTime: String): Int {
        return try {
            val hour1 = firstTime.split(":")[0].toInt()
            val hour2 = secondTime.split(":")[0].toInt()

            when {
                hour1 < hour2 -> -1  // hour1 is earlier
                hour1 > hour2 -> 1   // hour1 is later
                else -> 0            // hours are equal
            }
        } catch (e: Exception) {
            println("Error comparing hours: ${e.message}; the first parameter: $firstTime; the second parameter: $secondTime;")
            -2
        }
    }

    fun getDateAndTimeInTimezone(timezoneStr: String): String {
        val timezone = TimeZone.getTimeZone(timezoneStr)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateFormat.timeZone = timezone
        return dateFormat.format(Date())
    }

    fun convertDateToUserLocale(dateString: String): String {
        val originalFormat = SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH)
        val date: Date = originalFormat.parse(dateString)
            ?: throw IllegalArgumentException("Invalid date format: $dateString")
        val userFormat = SimpleDateFormat("MMM dd", Locale.ENGLISH)

        return userFormat.format(date)
    }

    fun getDayOfTheWeek(date: String): String {
        val format = SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH)
        val dateObj = format.parse(date)

        val calendar = Calendar.getInstance()
        calendar.time = dateObj

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayName = SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.time)

        return dayName
    }

    fun getCurrentDate(): String {
        val date = Date()
        val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val formattedDate = formatter.format(date)

        return formattedDate
    }

    fun getNextDayDate(date: String = getCurrentDate()): String {
        val format = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val calendar = Calendar.getInstance()

        val dateObj = format.parse(date)
        calendar.time = dateObj

        calendar.add(Calendar.DAY_OF_YEAR, 1)

        return format.format(calendar.time)
    }

    fun getCurrentTime(): String {
        val time = Date()
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormatter.format(time)

        return formattedTime
    }

    fun getCurrentHour(): Int {
        val time = Date()
        val timeFormatter = SimpleDateFormat("HH", Locale.getDefault())
        val formattedTime = timeFormatter.format(time)

        return formattedTime.toInt()
    }

}