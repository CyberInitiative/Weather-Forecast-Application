package com.example.weather.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weather.entity.CityEntity

@Dao
interface CityDao {

    @Query("SELECT * FROM city")
    suspend fun fetchAll(): List<CityEntity>

    @Query("SELECT * FROM city WHERE isHomeCity = 1")
    suspend fun fetchHomeCity(): CityEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cityEntity: CityEntity): Long

    @Update
    suspend fun update(cityEntity: CityEntity)

    @Query(
        "UPDATE city SET isHomeCity = :isHomeCity " +
                "WHERE name = :name " +
                "AND latitude = :latitude " +
                "AND longitude = :longitude"
    )
    suspend fun updateHomeCityStatus(
        isHomeCity: Boolean,
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