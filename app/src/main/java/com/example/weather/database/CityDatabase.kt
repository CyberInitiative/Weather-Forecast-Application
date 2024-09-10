package com.example.weather.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weather.dao.CityDao
import com.example.weather.model.City

@Database(entities = [City::class], version = 1, exportSchema = false)
abstract class CityDatabase: RoomDatabase() {
    abstract fun cities(): CityDao
}