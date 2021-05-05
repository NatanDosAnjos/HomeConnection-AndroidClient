package com.example.automatize.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.automatize.model.Device
import com.example.automatize.model.ServerToConnect

class MainViewModel(private var server: ServerToConnect): ViewModel() {

    fun getDevices(): LiveData<MutableList<Device>> {
        return server.devicesLiveData
    }

    fun changeStatus(deviceWithNewStatus: Device) {
        server.sendCommand(deviceWithNewStatus.topicCommand, deviceWithNewStatus.status)
    }

}