package com.example.automatize.model

class Device (
    val name: String,
    val topicCommand: String,
    val topicResponse: String,
    val topicWill: String,
    private val type: String) {

    var status = "l"

    //Static Property in Kotlin
    companion object {
        @JvmStatic val TOPIC_DEVICES_JSON = "casa/+/+/+/json"
        @JvmStatic val COMMAND_PULSE_THIS = "pulseThis"
        @JvmStatic val COMMAND_TURN_ON = "h"
        @JvmStatic val COMMAND_TURN_OFF = "l"
        @JvmStatic val TYPE_ONOFF = "onOff"
        @JvmStatic val TYPE_PULSE = "pulse"
    }

    fun isPulseType() : Boolean {
        return type == TYPE_PULSE
    }

    fun isOnOffType() : Boolean {
        return type == TYPE_ONOFF
    }
}