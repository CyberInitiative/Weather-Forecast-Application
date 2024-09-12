package com.example.weather.mapper

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object DateAndTimeMapper {
    private const val ISO8601_STANDARD_REGEX_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$"
    private const val TIME_PATTERN = "HH:mm"
    private const val DATE_PATTERN = "yyyy-MM-dd"

    fun splitDateAndTime(dateAndTime: String): Pair<String, String> {
        if (dateAndTime.matches(ISO8601_STANDARD_REGEX_PATTERN.toRegex())) {
            val (date, time) = dateAndTime.split("T")
            return Pair(date, time)
        } else {
            throw IllegalArgumentException("Expected date and time format YYYY-MM-DDThh:mm, but received: $dateAndTime")
        }
    }

    fun getDateAndTimeInTimezone(timezoneStr: String): String {
        val timezone = TimeZone.getTimeZone(timezoneStr)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateFormat.timeZone = timezone
        return dateFormat.format(Date())
    }

    fun getDayOfTheWeek(date: String): String{
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

    fun getCurrentTime(): String{
        val time = Date()
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormatter.format(time)

        return formattedTime
    }

    fun getCurrentHour(): Int{
        val time = Date()
        val timeFormatter = SimpleDateFormat("HH", Locale.getDefault())
        val formattedTime = timeFormatter.format(time)

        return formattedTime.toInt()
    }

}