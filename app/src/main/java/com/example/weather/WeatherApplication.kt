package com.example.weather

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.weather.database.CityDatabase
import com.example.weather.interceptor.CacheInterceptor
import com.example.weather.interceptor.ForceCacheInterceptor
import com.example.weather.network.NetworkManager
import com.example.weather.repository.CityRepository
import com.example.weather.repository.ForecastRepository
import com.example.weather.service.ForecastService
import com.example.weather.service.GeocodingService
import com.example.weather.viewmodel.CitySearchViewModel
import com.example.weather.viewmodel.ForecastViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

private val MODULE = module {
    single {
        Room.databaseBuilder(androidContext(), CityDatabase::class.java, "weather.db").build()
    }
    single { get<CityDatabase>().cities() }
    single { createCache(get()) }
    single { createOkHttpClient(get()) }
    single { getGeocodingApi() }
    single { getForecastApi(get()) }
    single { ForecastRepository(get()) }
    single { CityRepository(get(), get()) }
    single { NetworkManager(androidContext()) }
    viewModel { ForecastViewModel(get(), get()) }
    viewModel { CitySearchViewModel(get()) }
}

fun getGeocodingApi(): GeocodingService {
    return Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingService::class.java)
}

fun getForecastApi(okHttpClient: OkHttpClient): ForecastService {
    return Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ForecastService::class.java)
}

fun createCache(context: Context): Cache {
    val cacheSize = 10 * 1024 * 1024 // 10 MB
    val cacheDirectory = File(context.cacheDir, "http_cache")
    return Cache(cacheDirectory, cacheSize.toLong())
}

fun createOkHttpClient(cache: Cache): OkHttpClient {
    return OkHttpClient.Builder()
        .cache(cache)
        .addNetworkInterceptor(CacheInterceptor())
        .addInterceptor(ForceCacheInterceptor())
        .build()
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