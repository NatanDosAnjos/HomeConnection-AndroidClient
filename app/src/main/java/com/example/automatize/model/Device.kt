package com.example.automatize.model

class Device(val name: String,
             val topicCommand: String,
             val topicResponse: String,
             val topicWill: String,
             private val type: String) {

    val hashCode = this.hashCode()
    var status = COMMAND_OFF


    //Static Property in Kotlin
    companion object {
        @JvmStatic val TOPIC_DEVICES_JSON = "casa/+/+/+/json"
        @JvmStatic val COMMAND_ON = "h"
        @JvmStatic val COMMAND_OFF = "L"
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