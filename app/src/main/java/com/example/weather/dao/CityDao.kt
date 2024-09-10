package com.example.weather.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weather.model.City

@Dao
interface CityDao {

    @Query("SELECT * FROM cities")
    suspend fun loadAllCities(): List<City>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: City)

    @Update
    suspend fun update(city: City)

    @Delete
    suspend fun delete(city: City)
}