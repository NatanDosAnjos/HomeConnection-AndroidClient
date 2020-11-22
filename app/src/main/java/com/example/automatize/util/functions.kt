package com.example.automatize.util

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

fun timer(runWhenEndTime: Runnable, secondsInFuture: Int) {
    val date = Date()
    val initTime = date.time
    val timeInFuture = initTime + (secondsInFuture * 1000)

    while (true) {
        if (timeInFuture >= System.currentTimeMillis()) {
            Thread.sleep(1000)
            println("$timeInFuture - ${System.currentTimeMillis()}")
        } else {
            runWhenEndTime.run()
            break
        }
    }
}

fun parseToInt(text: String?): Int {
    return try {
        if (text.isNullOrEmpty()) {
            -1
        } else {
            val number = text.toInt()
            number
        }
    } catch (e: Exception) {
        -1
    }
}

fun dateAndTime(): String {
    val millisInLong = System.currentTimeMillis()
    val simpleDateFormat = SimpleDateFormat("dd/MM/yy - HH:mm:ss |")
    return simpleDateFormat.format(millisInLong)
}

fun separateArguments(textToSeparate: String, delimiter: Regex = Regex("=")): List<String> {
    return textToSeparate.split(delimiter)
}

fun catchMacAddress(list: List<String>? = null): String? {
    list?.forEach {
        if (isMacAddress(it)) {
            return it
        }
    }
    return null
}

private fun isMacAddress(text: String): Boolean {
    val regex = "^(([0-9a-fA-F]{2}):){5}([0-9a-fA-F]{2})$".toRegex()
    return regex.matches(text)
}


fun myIp(): String? {
    var ip = "0"
    try {
        val whatMyIp = URL("https://checkip.amazonaws.com")
        val input = BufferedReader(InputStreamReader(whatMyIp.openStream()))
        ip = input.readLine()

        return ip

    } catch (e: Exception) {
        e.printStackTrace()
        println("Erro ao resolver endereço global. Por favor verifique sua conexão com a Internet")
    }


    return null
}

//Verify if my Internal IP is same of my Global IP
fun globalIpsAreSame(hostIp: String?, myIp: String? = myIp()): Boolean {
    if (hostIp != null || myIp != null) {
        return myIp == hostIp
    }
    return false
}

fun changeUi(runnable: Runnable) {
    val handler = Handler(Looper.getMainLooper())
    handler.post {
        runnable.run()
    }
}



