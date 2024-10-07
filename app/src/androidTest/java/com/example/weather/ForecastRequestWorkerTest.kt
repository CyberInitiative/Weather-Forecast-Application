package com.example.weather

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.weather.worker.ForecastRequestWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForecastRequestWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp(){
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testForecastRequestWorker(){
        val worker = TestListenableWorkerBuilder<ForecastRequestWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is Result.Success)

//            assertThat(worker.cityRepository.loadAll(), hasSize(equalTo(3)))
        }
    }
}