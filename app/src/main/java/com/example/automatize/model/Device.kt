package com.example.automatize.model
import java.util.*

class Device(val name: String,
             val topicCommand: String,
             val topicResponse: String,
             val topicWill: String,
             val type: String) {

    //Static Property in Kotlin
    companion object {
        @JvmStatic val TOPIC_DEVICES_JSON = "casa/+/+/+/json"
        @JvmStatic val COMMAND_ON = "H"
        @JvmStatic val COMMAND_OFF = "L"
        @JvmStatic val TYPE_ONOFF = "onOff"
        @JvmStatic val TYPE_PULSE = "pulse"
    }

    var status: String = COMMAND_OFF
    get() {
        return if (field == null) {
            COMMAND_OFF
        } else {
            field.toUpperCase(Locale.ROOT)
        }
    }

    init {
        status = COMMAND_OFF
    }
}