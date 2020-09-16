package com.example.automatize.model


class Device(val ip: String) {

    var status: String? = null
    var hisServer: Server? = null


    fun turnOnDevice() {
        if (hisServer != null) {
            val server = hisServer!!
            status = server.sendCommand(ip,"unlock")
        }
    }

    fun turnOffDevice() {
        if (hisServer != null) {
            val server = hisServer!!
            status = server.sendCommand(ip,"unlock")
        }
    }

    fun upgradeStatus() {
        if (hisServer != null) {
            val server = hisServer!!
            status = server.sendCommand(ip)
        }
    }

}