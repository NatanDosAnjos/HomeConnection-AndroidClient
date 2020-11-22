package com.example.automatize.model

import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerListener() : Thread() {

    private val forReceivePort = 2510
    var lineJson = "Nothing"


    private fun listenServer(socket: Socket) {
        val scan = Scanner(socket.inputStream)

        synchronized(lineJson) {
            if (scan.hasNextLine()) {
                lineJson = scan.nextLine()
            }
        }

    }

    private fun connect() : ServerSocket? {
        return try {
            ServerSocket(forReceivePort)

        } catch (e: Exception) {
            sleep(350)
            null
        }
    }

    override fun run() {
        super.run()

        while (true) {
            val internalServerForReceive = connect()

            if (internalServerForReceive != null) {
                while (!internalServerForReceive.isClosed) {

                    val socket = internalServerForReceive.accept()
                    println("o ip do Socket: ${socket.inetAddress}")
                    listenServer(socket)
                }
            } else {
                continue
            }
        }
    }
}