package com.example.automatize.model

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.automatize.activity.SettingActivity
import com.example.automatize.handler.MqttCallbackHandler
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
        val PASSWORD_KEY_NAME = "password"

        @JvmStatic
        val LOCAL_IP_KEY_NAME = "ip"

        @JvmStatic
        val USER_KEY_NAME = "user"

        @JvmStatic
        val PORT_KEY_NAME = "port"

        @JvmStatic
        val DNS_KEY_NAME = "dns"

        @JvmStatic
        val devicesList = mutableListOf<Device>()
    }

    private lateinit var sharedPreferencesListener: SharedPreferences.OnSharedPreferenceChangeListener
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    lateinit var qualityOfServiceArray: IntArray
    lateinit var subscribeArray: Array<String>
    private val mqttConnectOptions = MqttConnectOptions()
    val clientMqtt: MqttAndroidClient
    var runnable = Runnable { }
    var connected: Boolean = false
    private var serverLocalIp = prefs.getString(LOCAL_IP_KEY_NAME, "null").toString()
        set(value) {
            field = value
            if (value.isNotEmpty())
                changeServerURIs(localIp = value, dns = "n")
                reconnectAll(value)
        }
    private var serverDns = prefs.getString(DNS_KEY_NAME, "null").toString()
        set(value) {
            field = value
            if (value.isNotEmpty())
                changeServerURIs(localIp = "n", dns = value)
                reconnectAll(value)
        }
    private var password = prefs.getString(PASSWORD_KEY_NAME, "null").toString()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                mqttConnectOptions.password = value.toCharArray()
                reconnectAll(value)
            }
        }
    private var user = prefs.getString(USER_KEY_NAME, "null").toString()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                mqttConnectOptions.userName = value
                reconnectAll(value)
            }
        }
    private var port = prefs.getString(PORT_KEY_NAME, "null").toString()
        set(value) {
            field = value
            if (value.isNotEmpty())
                reconnectAll(value)
        }
    private val serverURIs = mutableListOf<String>("$serverLocalIp:$port", "$serverDns:$port")


    init {
        registerSharedPreferencesListener()
        try {
            mqttConnectOptions.serverURIs = serverURIs.toTypedArray()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            changeUi(Runnable {
                Snackbar.make(myView, "IP Local ou DNS informados são inválidos", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Alterar") {
                        context.startActivity(Intent(context, SettingActivity::class.java))
                    }
                    .show()
            })
        }
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.connectionTimeout = 2
        mqttConnectOptions.isCleanSession = false

        clientMqtt = MqttAndroidClient(context, "$serverLocalIp:$port", CLIENT_ID)
        clientMqtt.setCallback(MqttCallbackHandler(context))
    }


    private fun changeServerURIs(localIp: String, dns: String) {
        if (localIp != "n") {
            serverURIs[0] = localIp
        }

        if (dns != "n") {
            serverURIs[1] = dns
        }
        //TO REMOVE
        println(serverURIs.toString())
    }


    fun reconnectAll(value: String = "") {
        if (clientMqtt.isConnected) {
            clientMqtt.disconnectForcibly(1)
            connect()
        } else {
            connect()
        }
    }


    private fun connect() {
        clientMqtt.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Toast.makeText(context, "Conectado", Toast.LENGTH_SHORT).show()
                subscribeOnTopics()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.i("MQTT_CONNECTION", "Falha ao Conectar", exception)
                connected = false
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


    private fun registerSharedPreferencesListener() {
        sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {

                LOCAL_IP_KEY_NAME -> {
                    serverLocalIp = key
                }
                DNS_KEY_NAME -> {
                    serverDns = key
                }
                PASSWORD_KEY_NAME -> password = key
                USER_KEY_NAME -> user = key
                PORT_KEY_NAME -> port = key

            }
        }
        prefs.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
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

