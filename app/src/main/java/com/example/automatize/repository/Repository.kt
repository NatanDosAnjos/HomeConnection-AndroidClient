package com.example.automatize.repository

import com.example.automatize.model.Device

interface Repository {

    fun getDevices(): MutableList<Device>
    fun getState(): Device
    fun setState(topicCommand: String, command: String)
}