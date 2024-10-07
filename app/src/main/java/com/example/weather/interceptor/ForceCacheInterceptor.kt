package com.example.weather.interceptor

import android.util.Log
import com.example.weather.network.NetworkManager
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ForceCacheInterceptor : Interceptor, KoinComponent {
    private val networkManager: NetworkManager by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder: Request.Builder = request.newBuilder()

        if (!networkManager.isInternetAvailable()) {
            Log.d(TAG, "No internet connection.")
            val cacheControl = CacheControl.Builder()
                .onlyIfCached()
                .maxStale(7, TimeUnit.DAYS)
                .build()

            builder.cacheControl(cacheControl)
        }

        return chain.proceed(builder.build())
    }

    companion object{
        private const val TAG = "ForceCacheInterceptor"
    }
}