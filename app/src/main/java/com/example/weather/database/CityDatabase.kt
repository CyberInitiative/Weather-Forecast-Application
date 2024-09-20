package com.example.weather.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weather.dao.CityDao
import com.example.weather.entity.CityEntity

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class CityDatabase: RoomDatabase() {
    abstract fun cities(): CityDao
}