package com.example.automatize.model

import android.content.Context
import android.content.SharedPreferences
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.automatize.connection.Server
import com.example.automatize.handler.MqttCallbackHandler
import com.example.automatize.helper.ConnectionHelper
import com.example.automatize.repository.PrefsConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import java.net.InetAddress
import java.util.*


class ServerToConnect(val context: Context) {

    private val devicesList = mutableListOf<Device>()
    val devicesLiveData = MutableLiveData<MutableList<Device>>()
    private var receivedMessage: MutableLiveData<String> = MutableLiveData()
    private var receivedDevice: MutableLiveData<Device?> = MutableLiveData()
    private lateinit var sharedPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener
    private val prefsConfig = PrefsConfig()
    private val mqttConnectOptions: MqttConnectOptions = MqttConnectOptions()
    private var oldConnectionTypeName = ""

    // To Server Object
    private val callback = MqttCallbackHandler(context)
    private var server: Server
    private lateinit var clientMqtt: MqttAndroidClient
    private var connectionHel = ConnectionHelper(context)
    private var serverLocalIp = prefsConfig.getValueOfPreferences(PrefsConfig.LOCAL_IP_KEY_NAME)
    private var serverGlobalIP = prefsConfig.getValueOfPreferences(PrefsConfig.GLOBAL_IP_KEY_NAME)
    private var password = prefsConfig.getValueOfPreferences(PrefsConfig.PASSWORD_KEY_NAME)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                mqttConnectOptions.password = value.toCharArray()
            }
        }
    private var user = prefsConfig.getValueOfPreferences(PrefsConfig.USER_KEY_NAME)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                mqttConnectOptions.userName = value
            }
        }
    private var port = prefsConfig.getValueOfPreferences(PrefsConfig.PORT_KEY_NAME)
    private var ip: String = serverLocalIp


    init {
        registerSharedPreferencesListener()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.connectionTimeout = 2
        mqttConnectOptions.isCleanSession = false
        server = Server(context, mqttConnectOptions, ip, port)
        callback.run {
            onConnectRunnable = Runnable {
                subscribeOnTopics()
            }
            onDisconnectRunnable = Runnable {

            }
        }
        initConnectionHelperAndConnect()
    }


    private fun initConnectionHelperAndConnect() {
        connectionHel.getConnectionLiveData().observeForever {
            if (!(it.equals(oldConnectionTypeName))) {
                oldConnectionTypeName = it
                if (it == ConnectionHelper.WIFI_CONNECTION_KEY) {
                    ip = serverLocalIp
                    CoroutineScope(Dispatchers.IO).launch {
                        isItOnTheSameNetworkAsTheServer()
                    }
                    Toast.makeText(context, "WiFi", Toast.LENGTH_SHORT).show()
                    connectWithNewURL()

                } else if (it == ConnectionHelper.MOBILE_CONNECTION_KEY) {
                    ip = serverGlobalIP
                    connectWithNewURL()
                    Toast.makeText(context, "mobile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // verifica se está na mesma rede que o servidor.
    private fun isItOnTheSameNetworkAsTheServer() {
        val localIp = serverLocalIp.removePrefix(PrefsConfig.PROTOCOL_PREFIX)
        val address = InetAddress.getByName(localIp)
        val isConnected = address.isReachable(25)
        if (!isConnected) {
            ip = serverGlobalIP
            connectWithNewURL()
        }
    }

    private fun connectWithNewURL() {
        unsubscribeOnTopics(devicesList.toTypedArray())
        server.disconnect()
        server = Server(context, mqttConnectOptions, ip, port)
        server.callback = callback
        clientMqtt = server.connect()
    }


    private fun subscribeOnTopics() {
        clientMqtt.subscribe(Device.TOPIC_DEVICES_JSON, 2) { topic, message ->
            Log.println(Log.DEBUG, "ServerToConnect - 103", topic.toString())
            Log.println(Log.INFO, "ServerToConnect - 104: ", message.toString())
            if (message.toString()
                    .toUpperCase(Locale.ENGLISH) == Device.COMMAND_OFF || message.toString()
                    .isEmpty()
            ) {
                val deviceIndex = searchDeviceByTopicWillOrTopicResponse(topic)
                if (deviceIndex != -1) {
                    val deviceToDelete = devicesList[deviceIndex]

                    clientMqtt.unsubscribe(
                        arrayOf(
                            deviceToDelete.topicResponse,
                            deviceToDelete.topicWill
                        )
                    )
                    updateDevicesList(deviceToDelete, true)
                }

            } else {
                try {
                    val device = Gson().fromJson(message.toString(), Device::class.java)
                    if (!deviceExist(device)) {
                        subscribeOnResponseCommand(device)
                        updateDevicesList(device)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun unsubscribeOnTopics(devicesList: Array<Device>) {
        for (device in devicesList) {
            Log.i("Server-138", "Tópico a ser desinscrito: ${device.topicResponse}")
            try {
                clientMqtt.unsubscribe(device.topicResponse)

            } catch (e: MqttException) {
                e.printStackTrace()
            }
            clearDevicesList(devicesList)
        }
    }

    private fun clearDevicesList(devicesList: Array<Device>) {
        this.devicesList.removeAll(devicesList)
        devicesLiveData.postValue(this.devicesList)
    }


    private fun subscribeOnResponseCommand(device: Device) {
        receivedDevice = MutableLiveData(device)
        clientMqtt.subscribe(device.topicResponse, 2) { topic, message ->
            receivedMessage = MutableLiveData("$topic+$message")
            val deviceIndex = searchDeviceByTopicWillOrTopicResponse(topic)
            if (deviceIndex != -1) {
                val thisDevice = devicesList[deviceIndex]
                thisDevice.status = message.toString()
                devicesList[deviceIndex] = thisDevice
                updateStatus()

            } else {
                for ((i, d) in devicesList.withIndex()) {
                    println(" ${d.name}  [$i] ")
                }
            }
        }
    }


    fun sendCommand(topic: String, payload: String) {
        clientMqtt.publish(topic, payload.toByteArray(), 1, false)
    }


    private fun deviceExist(deviceToCompare: Device): Boolean {
        for (deviceInList in devicesList) {
            if (deviceInList.topicWill == deviceToCompare.topicWill) {
                return true
            }
        }

        return false
    }


    private fun searchDeviceByTopicWillOrTopicResponse(topic: String): Int {
        for ((index, device) in devicesList.withIndex()) {
            if (device.topicWill == topic || device.topicResponse == topic) {
                return index
            }
        }

        return -1
    }


    private fun updateDevicesList(device: Device, toRemove: Boolean = false) {

        if (!toRemove) {
            devicesList.add(device)
            devicesLiveData.postValue(devicesList)

        } else {
            val indexToRemove = getDevicePositionOnDevicesList(device, devicesList)
            if (indexToRemove >= 0) {
                unsubscribeOnTopics(arrayOf(devicesList[indexToRemove]))
                devicesList.removeAt(indexToRemove)
                devicesLiveData.postValue(devicesList)
            }
        }
        printDevicesListName(devicesList)
    }

    private fun updateStatus(list: MutableList<Device> = devicesList) {
        devicesLiveData.postValue(list)
    }

    private fun getDevicePositionOnDevicesList(device: Device, list: MutableList<Device>): Int {
        for ((index, deviceInList) in list.withIndex()) {
            if (deviceInList.name == device.name) {
                return index
            }
        }
        return -1
    }


    private fun registerSharedPreferencesListener() {
        sharedPrefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedP, key ->
            when (key) {

                PrefsConfig.LOCAL_IP_KEY_NAME -> {
                    serverLocalIp = sharedP.getString(key, null)!!
                    Log.i("Server-229", serverLocalIp)
                }
                PrefsConfig.PASSWORD_KEY_NAME -> {
                    password = sharedP.getString(key, null)!!
                }
                PrefsConfig.GLOBAL_IP_KEY_NAME -> {
                    serverGlobalIP = sharedP.getString(key, null)!!
                }
                PrefsConfig.USER_KEY_NAME -> {
                    user = sharedP.getString(key, null)!!
                }
                PrefsConfig.PORT_KEY_NAME -> {
                    port = sharedP.getString(key, null)!!
                }
            }
            connectionHel.whichConnectionType()
            reassignIpVariable(connectionHel.whichConnectionType())
            connectWithNewURL()
        }
        PrefsConfig().registerListener(sharedPrefsListener)
    }

    private fun reassignIpVariable(networkTransportNumber: Int) {
        if (networkTransportNumber == NetworkCapabilities.TRANSPORT_WIFI) {
            ip = serverLocalIp
        } else if (networkTransportNumber == NetworkCapabilities.TRANSPORT_CELLULAR) {
            ip = serverGlobalIP
        }
    }

    private fun printDevicesListName(list: MutableList<Device>?) {
        print("[")
        if (list != null) {
            for (device in list) {
                print("${device.name},")
            }
        }
        println("]")
    }
}




