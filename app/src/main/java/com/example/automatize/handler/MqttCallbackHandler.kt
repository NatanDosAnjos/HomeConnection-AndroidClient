package com.example.automatize.handler

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttCallbackHandler(private val context: Context, private val showOnDisconnectionToast: Boolean = true) : MqttCallbackExtended {
    private val tag = "MQTT_Callback_Handler"
    var onDisconnectRunnable = Runnable {  }
    var onConnectRunnable = Runnable {  }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        onConnectRunnable.run()
        showToast(context, "Conectado")
        Log.i(tag, "Connected to: $serverURI")
    }

    override fun connectionLost(cause: Throwable?) {
        onDisconnectRunnable.run()
        if (showOnDisconnectionToast) {
            //showToast(context, "Desconectado")
        }
        Log.i(tag, "Connection Lost", cause)

    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.i(tag, "Message \"$message\" arrived  in topic \"$topic\"  ")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.i(tag, "delivery Complete")
    }


    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}