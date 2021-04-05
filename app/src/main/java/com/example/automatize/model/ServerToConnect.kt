package com.example.automatize.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.automatize.handler.MqttCallbackHandler
import com.example.automatize.repository.PrefsConfig
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.util.*

class ServerToConnect {

    companion object {
       @JvmStatic lateinit var context: Context
    }

    private val devicesList = mutableListOf<Device>()
    val devicesLiveData = MutableLiveData<MutableList<Device>>()
    //private val devicesList = mutableListOf<Device>()

    private var receivedMessage: MutableLiveData<String> = MutableLiveData()
    private var receivedDevice: MutableLiveData<Device?> = MutableLiveData()

    private val CLIENT_ID = MqttClient.generateClientId()!!


    private lateinit var sharedPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener
    private val mqttConnectOptions = MqttConnectOptions()
    private var clientMqtt: MqttAndroidClient
    private val prefsConfig = PrefsConfig()


    private var serverLocalIp =
        prefsConfig.getValueOfPreferences(context, PrefsConfig.LOCAL_IP_KEY_NAME)
        set(value) {
            field = value
            /*if (value.isNotEmpty()) {
                //changeServerURIs(value, serverDns)
            }*/
        }

    private var password = prefsConfig.getValueOfPreferences(context, PrefsConfig.PASSWORD_KEY_NAME)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                mqttConnectOptions.password = value.toCharArray()
            }
        }
    private var user = prefsConfig.getValueOfPreferences(context, PrefsConfig.USER_KEY_NAME)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                mqttConnectOptions.userName = value
            }
        }
    private var port = prefsConfig.getValueOfPreferences(context, PrefsConfig.PORT_KEY_NAME)
    //private val serverURIs = mutableListOf("$serverLocalIp:$port", "$serverDns:$port")


    init {
        try {
            //mqttConnectOptions.serverURIs = serverURIs.toTypedArray()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            //showSnackbar("IP ou DNS são inválidos")
        }
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.connectionTimeout = 2
        mqttConnectOptions.isCleanSession = false

        clientMqtt = MqttAndroidClient(context, "$serverLocalIp:$port", CLIENT_ID)

        registerSharedPreferencesListener()
        connect()
    }


    /*private fun changeServerURIs(localIp: String, dns: String) {
        serverURIs[0] = "$localIp:$port"
        serverURIs[1] = "$dns:$port"
        mqttConnectOptions.serverURIs = serverURIs.toTypedArray()
        reconnect()

    }*/


    private fun connect() {
        val callback = MqttCallbackHandler(context)

        clientMqtt = MqttAndroidClient(context, "$serverLocalIp:$port", CLIENT_ID)
        clientMqtt.setCallback(callback)

        clientMqtt.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                subscribeOnTopics()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                //showSnackbar("Falha ao conectar com ${asyncActionToken?.client?.serverURI}")
                Log.i("MQTT_CONNECTION", "Falha ao Conectar", exception)
            }
        })
    }

    private fun subscribeOnTopics() {
        clientMqtt.subscribe(Device.TOPIC_DEVICES_JSON, 2) { topic, message ->
            if (message.toString().toUpperCase(Locale.ENGLISH) == Device.COMMAND_OFF) {
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
                    println("A Menssagem vinda do Broker: $message")
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
                println("device não existe na lista")
                for ((i, d) in devicesList.withIndex()) {
                    println(" ${d.name}  [$i] ")
                }
            }
        }
    }


    fun sendCommand(topic: String, payload: String) {
        clientMqtt.publish(topic, payload.toByteArray(), 1, false )
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
                }
                PrefsConfig.PASSWORD_KEY_NAME -> {
                    password = sharedP.getString(key, null)!!
                }
                PrefsConfig.DNS_KEY_NAME -> {
                    //serverDns = sharedP.getString(key, null)!!
                }
                PrefsConfig.USER_KEY_NAME -> {
                    user = sharedP.getString(key, null)!!
                }
                PrefsConfig.PORT_KEY_NAME -> {
                    port = sharedP.getString(key, null)!!
                }
            }
        }

        PrefsConfig().registerListener(context, sharedPrefsListener)
    }

    private fun printDevicesListName(list: MutableList<Device>?) {
        print("[")
        if (list != null) {
            for(device in list) {
                print("${device.name},")
            }
        }
        println("]")
    }

    /*private fun serverGlobalIp(): String? {
        return try {
            val inetAddressObject = InetAddress.getByName(serverDns)
            val ip: String? = inetAddressObject.hostAddress
            ip

        } catch (e: Exception) {
            null
        }
    }*/
}

