package com.example.weather.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.weather.entity.CityEntity

@Dao
interface CityDao {

    @Query("SELECT * FROM city")
    suspend fun fetchAll(): List<CityEntity>

    @Query("SELECT * FROM city WHERE isCurrentCity = 1")
    suspend fun fetchCurrentCity(): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cityEntity: CityEntity): Long

    @Update
    suspend fun update(cityEntity: CityEntity)

    @Query(
        "UPDATE city SET isCurrentCity = :isCurrentCity " +
                "WHERE name = :name " +
                "AND latitude = :latitude " +
                "AND longitude = :longitude"
    )
    suspend fun updateCurrentCityStatus(
        isCurrentCity: Boolean,
        name: String,
        latitude: Double,
        longitude: Double
    )

    @Delete
    suspend fun delete(cityEntity: CityEntity)

    @Query(
        "DELETE FROM city " +
                "WHERE name = :name " +
                "AND latitude = :latitude " +
                "AND longitude = :longitude"
    )
    suspend fun deleteByCityParameters(
        name: String,
        latitude: Double,
        longitude: Double
    )
}