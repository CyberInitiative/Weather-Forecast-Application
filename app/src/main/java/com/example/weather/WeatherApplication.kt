package com.example.weather

import android.app.Application
import androidx.room.Room
import com.example.weather.database.CityDatabase
import com.example.weather.repository.CityRepository
import com.example.weather.repository.ForecastRepository
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val MODULE = module {
    single { Room.databaseBuilder(androidContext(), CityDatabase::class.java, "city.db").build() }
    single { get<CityDatabase>().cities() }
    single { ForecastRepository() }
    single { CityRepository(get()) }
    viewModel { ForecastViewModel(get(), get()) }
}

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@WeatherApplication)
            modules(MODULE)
        }
    }
}