package com.example.weather.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkManager(context: Context) {
    val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun getNetworkCallback(
        onAvailable: (() -> Unit)?,
        onUnavailable: (() -> Unit)?,
        onLost: (() -> Unit)?
    ): NetworkCallback {
        return object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (onAvailable != null) {
                    onAvailable()
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                if (onUnavailable != null) {
                    onUnavailable()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                if (onLost != null) {
                    onLost()
                }
            }
        }
    }

    fun registerNetworkCallback(networkRequest: NetworkRequest, networkCallback: NetworkCallback) {
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun unregisterNetworkCallback(networkCallback: NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}