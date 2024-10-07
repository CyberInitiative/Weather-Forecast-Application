package com.example.weather

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.weather.database.CityDatabase
import com.example.weather.entity.CityEntity
import com.example.weather.repository.CityRepository
import com.example.weather.service.GeocodingService
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CityRepositoryTest {
    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        CityDatabase::class.java
    ).build()

    private val cityDao = database.cities()

    private val geocodingService = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingService::class.java)

    private val cityRepository: CityRepository = CityRepository(cityDao, geocodingService)

    private val firstCityEntity = CityEntity(
        1111,
        "First City",
        "First Country",
        11.111,
        22.222,
        "1admin1",
        "1admin2",
        "1admin3",
        "1admin4"
    )

    private val secondCityEntity = CityEntity(
        2222,
        "Second City",
        "Second Country",
        33.333,
        44.444,
        "2admin1",
        "2admin2",
        "2admin3",
        "2admin4"
    )

    @Before
    fun prepareDatabase() = runBlocking {
        assertThat(cityDao.fetchAll(), empty())

        cityDao.insert(firstCityEntity)
        cityDao.insert(secondCityEntity)

        cityDao.fetchAll().let {
            assertThat(it, hasSize(equalTo(2)))
            assertThat(it[0], equalTo(firstCityEntity))
            assertThat(it[1], equalTo(secondCityEntity))
        }
    }

    @Test
    fun testFetchMethod(): Unit = runBlocking{
        cityRepository.loadAll().let {
            assertThat(it, hasSize(equalTo(2)))
            assertThat(it[0], equalTo(firstCityEntity.mapToDomain()))
            assertThat(it[1], equalTo(secondCityEntity.mapToDomain()))
        }
    }

}