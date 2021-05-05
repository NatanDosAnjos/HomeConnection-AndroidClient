package com.example.automatize.connection

import android.content.Context
import android.util.Log
import com.example.automatize.handler.MqttCallbackHandler
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

class Server(var context: Context, var mqttConnectOptions: MqttConnectOptions, var ip: String, var port: String) {

    var callback = MqttCallbackHandler(context)
    private var clientID = MqttClient.generateClientId()!!
    private var clientMqtt: MqttAndroidClient = MqttAndroidClient(context, "$ip:$port", this.clientID)
    lateinit var mqttToken: IMqttToken
        private set

    fun connect(): MqttAndroidClient {

        clientMqtt = MqttAndroidClient(context, "$ip:$port", this.clientID)
        clientMqtt.setCallback(callback)

        mqttToken = clientMqtt.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.println(Log.INFO, "ServerToConnect - 111", "Conectado")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.i("ServerToConnect", "Falha ao Conectar", exception)
            }
        })

        return clientMqtt
    }

    fun disconnect() {
        Log.i("Server-114-URL", clientMqtt.serverURI)
        if (clientMqtt.isConnected) {
            clientMqtt.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i("Server", "Desconexão Com Sucesso")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.i("Server", "Desconexão Falhou")
                }

            })
            Log.i("Server-118", "Disconnected")
        } else {
            Log.i("Server-118", "Não estava conectado")
        }
    }
}