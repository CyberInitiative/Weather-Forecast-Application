package com.example.weather.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weather.forecastApiForRepositoryQualifier
import com.example.weather.repository.CityRepository
import com.example.weather.service.ForecastService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ForecastRequestWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    context,
    workerParams
), KoinComponent {

    private val api: ForecastService by inject(forecastApiForRepositoryQualifier)
    private val cityRepository: CityRepository by inject()

    override suspend fun doWork(): Result {
//        val cities = cityRepository.loadAll()
//
//        return withContext(Dispatchers.IO) {
//            val jobs = mutableListOf<Job>()
//            for (city in cities) {
//                jobs.add(launch {
//                    api.loadForecast(city.latitude, city.longitude, timezone = city.timezone)
//                })
//            }
//            jobs.joinAll()

            return Result.success()
//        }
    }
}