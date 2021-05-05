package com.example.automatize.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ConnectionHelper(private val context: Context) {
    companion object {
        const val MOBILE_CONNECTION_KEY = "Mobile"
        const val WIFI_CONNECTION_KEY = "WiFi"
    }

    private var connectionState = MutableLiveData<String>()

    fun getConnectionLiveData(): LiveData<String> = connectionState

    init {
        wifiIsConnected()
    }

    private fun wifiIsConnected() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network : Network) {

            }

            override fun onLost(network : Network) {

            }

            override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
                if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    connectionState.postValue(WIFI_CONNECTION_KEY)

                } else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    connectionState.postValue(MOBILE_CONNECTION_KEY)

                }
            }

            override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {

            }
        })
    }

    fun whichConnectionType(): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if(capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            return NetworkCapabilities.TRANSPORT_WIFI

        } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
            return NetworkCapabilities.TRANSPORT_CELLULAR
        }

        return -1
    }
}