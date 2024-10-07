package com.example.weather

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.weather.database.CityDatabase
import com.example.weather.entity.CityEntity
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        CityDatabase::class.java
    ).build()

    private val underTest = database.cities()

    @Test
    fun insertAndLoad() = runBlocking {
        assertThat(underTest.fetchAll(), empty())

        val cityEntity = CityEntity(
            1111,
            "Rio",
            "Brazil",
            11.111,
            22.222,
            "admin1",
            "admin2",
            "admin3",
            "admin4"
        )

        underTest.insert(cityEntity)

        underTest.fetchAll().let {
            assertThat(it, hasSize(equalTo(1)))
            assertThat(it[0], equalTo(cityEntity))
        }
    }
}