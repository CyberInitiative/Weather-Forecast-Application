package com.example.weather.mapper

object DateAndTimeMapper {
    private const val ISO8601_STANDARD_REGEX_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$"

    fun splitDateAndTime(dateAndTime: String): Pair<String, String> {
        if (dateAndTime.matches(ISO8601_STANDARD_REGEX_PATTERN.toRegex())) {
            val (date, time) = dateAndTime.split("T")
            return Pair(date, time)
        } else {
            throw IllegalArgumentException("Expected date and time format YYYY-MM-DDThh:mm, but received: $dateAndTime")
        }
    }

}