package com.example.automatize.model

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.automatize.activity.SettingActivity
import com.example.automatize.handler.MqttCallbackHandler
import com.example.automatize.helper.PrefsConfig
import com.example.automatize.util.changeUi
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

class ServerToConnect(private val context: Context, private val myView: View) {

    companion object {
        @JvmStatic
        val CLIENT_ID = MqttClient.generateClientId()!!

        @JvmStatic
        val devicesList = mutableListOf<Device>()
    }

    private lateinit var sharedPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var qualityOfServiceArray: IntArray
    lateinit var subscribeArray: Array<String>
    private val mqttConnectOptions = MqttConnectOptions()
    val clientMqtt: MqttAndroidClient
    private val prefsConfig = PrefsConfig()
    var runnable = Runnable { }


    private var serverLocalIp = prefsConfig.getValueOfPreferences(context, PrefsConfig.LOCAL_IP_KEY_NAME)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                changeServerURIs(value, serverDns)
            }
        }

    private var serverDns = prefsConfig.getValueOfPreferences(context, PrefsConfig.DNS_KEY_NAME)

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
    private val serverURIs = mutableListOf("$serverLocalIp:$port", "$serverDns:$port")



    init {
        try {
            mqttConnectOptions.serverURIs = serverURIs.toTypedArray()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            showSnackbar("IP ou DNS são inválidos")
        }
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.connectionTimeout = 2
        mqttConnectOptions.isCleanSession = false

        clientMqtt = MqttAndroidClient(context, "$serverLocalIp:$port", CLIENT_ID)
        clientMqtt.setCallback(MqttCallbackHandler(context))
        connect()
    }


    private fun changeServerURIs(localIp: String, dns: String) {
        serverURIs[0] = "$localIp:$port"
        serverURIs[1] = "$dns:$port"
        mqttConnectOptions.serverURIs = serverURIs.toTypedArray()


    }


    private fun connect() {
        clientMqtt.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Toast.makeText(context, "Conectado", Toast.LENGTH_SHORT).show()
                subscribeOnTopics()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                showSnackbar("Falha ao conectar com ${asyncActionToken?.client?.serverURI}")
                Log.i("MQTT_CONNECTION", "Falha ao Conectar", exception)
            }
        })
    }


    private fun subscribeOnTopics() {
        subscribeArray
        qualityOfServiceArray


        clientMqtt.subscribe(Device.TOPIC_DEVICES_JSON, 2) { topic, message ->
            if (message.toString() == "l" || message.toString() == "L") {
                val deviceToDelete = searchDeviceFromTopicWillOrTopicResponse(topic)
                if (deviceToDelete != null) {
                    clientMqtt.unsubscribe(arrayOf(deviceToDelete.topicResponse, deviceToDelete.topicWill))
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


    private fun subscribeOnResponseCommand(device: Device) {
        clientMqtt.subscribe(device.topicResponse, 2) { topic, message ->
            val thisDevice = searchDeviceFromTopicWillOrTopicResponse(topicResponse = topic)
            thisDevice?.status = message.toString()
            println("$message ${thisDevice?.name} = ${thisDevice?.status}")
            runnable.run()

        }
    }

    private fun deviceExist(deviceToCompare: Device): Boolean {
        for (deviceInList in devicesList) {
            if (deviceInList.topicWill == deviceToCompare.topicWill) {
                return true
            }
        }

        return false
    }


    private fun searchDeviceFromTopicWillOrTopicResponse(topicWill: String = " ", topicResponse: String = " "): Device? {
        for (device in devicesList) {
           if (device.topicWill == topicWill) {
                return device
            } else if (device.topicResponse == topicResponse) {
                return device
            }
        }

        return null
    }


    private fun updateDevicesList(device: Device, toRemove: Boolean = false) {
        if (!toRemove) {
            devicesList.add(device)
        } else {
            devicesList.remove(device)
        }
        runnable.run()
    }


    fun registerSharedPreferencesListener(additionalRunnable: Runnable  = Runnable {}) {
        sharedPrefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedP, key ->
            when (key) {

                PrefsConfig.LOCAL_IP_KEY_NAME -> {
                    serverLocalIp = sharedP.getString(key, null)!!
                }
                PrefsConfig.PASSWORD_KEY_NAME -> {
                    password = sharedP.getString(key, null)!!
                }
                PrefsConfig.DNS_KEY_NAME -> {
                    serverDns = sharedP.getString(key, null)!!
                }
                PrefsConfig.USER_KEY_NAME -> {
                    user = sharedP.getString(key, null)!!
                }
                PrefsConfig.PORT_KEY_NAME -> {
                    port = sharedP.getString(key, null)!!
                }
            }
            additionalRunnable.run()
        }

        PrefsConfig().registerListener(context, sharedPrefsListener)
    }

    private fun showSnackbar(message: String) {
        changeUi(Runnable {
            Snackbar.make(myView, message, Snackbar.LENGTH_LONG)
                .setAction("Alterar") {
                    context.startActivity(Intent(context, SettingActivity::class.java))
                }.show()
        })
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

