package com.example.automatize.model

import java.io.InputStream
import java.lang.Exception
import java.net.Socket
import java.util.*


class Server (private val host: String, private val port: Int) {

    private fun connect(): Socket? {
        return try {
            Socket(host, port)

        } catch (e: Exception) {
            null
        }
    }

    fun sendCommand(deviceIp: String, command: String = "unlock"): String? {
        val serverSocket = connect()

        if(serverSocket != null) {
            val output = serverSocket.outputStream
            output.write("$deviceIp=$command\n".toByteArray())

            return receiveStatus(serverSocket.inputStream)
        }

        return ""
    }

    private fun receiveStatus(input: InputStream): String{
        val scan = Scanner(input)

        if (scan.hasNext()) {
            return scan.nextLine()
        }

        return "CANT READ STATUS"
    }


}