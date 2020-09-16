package com.example.automatize

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.automatize.model.Device
import com.example.automatize.model.Server
import com.example.automatize.util.MyTimer
import kotlinx.coroutines.Runnable
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.URL
import java.util.*
import kotlin.concurrent.thread



class MainActivity : AppCompatActivity() {

    private val PORT = 2706
    private val SERVERDNSADDRESS = "natanraspserver.ddns.net"
    private val SERVERLOCALIP =  "192.168.15.39"
    var timer: MyTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.button)
        val btnTime = findViewById<Button>(R.id.btnTime)

        val myRunnableTodo = Runnable {
            when (btn.text) {
                "ON" -> doIt("192.168.15.46", false)
                "OFF" -> doIt("192.168.15.46",true)
                else -> doIt("192.168.15.46")
            }
            btnTime.text = getString(R.string.button_time)
        }


        btn.setOnClickListener {
            when (btn.text) {
                "ON" -> doIt("192.168.15.46", false)
                "OFF" -> doIt("192.168.15.46",true)
                else -> doIt("192.168.15.46")
            }
        }

        btn.setOnLongClickListener {
            if (btnTime.visibility == View.VISIBLE) {
                btnTime.visibility = View.INVISIBLE
                findViewById<EditText>(R.id.editText).visibility = View.INVISIBLE
            } else {
                btnTime.visibility = View.VISIBLE
                findViewById<EditText>(R.id.editText).visibility = View.VISIBLE
            }
            return@setOnLongClickListener true
        }

        btnTime.setOnClickListener {

            val timeEditText = findViewById<EditText>(R.id.editText)
            val text = timeEditText.text.toString()

            if(timer == null || !timer!!.isRunning && timeEditText.text.isEmpty()) {
                timer = MyTimer(this, btnTime,text.toLong()*1000, runWhenTimeFinished = myRunnableTodo)
                timeEditText.setText("")
                timer!!.start()

            } else if(timer!!.isRunning) {
                timer!!.isRunning = false
                timer!!.cancel()
                btnTime.text = getString(R.string.button_time)
            }
        }
    }

    /*override fun onResume() {
        super.onResume()
        doIt("192.168.15.46")
    }*/


    /*If chageStatus is equals to null the command for receive device status will be send*/
    private fun doIt(ipDevice: String, changeStatusTo: Boolean? = null) {
        thread {
            try {
                val server: Server = if (ipsAreSame(myIp(), hostIp(SERVERDNSADDRESS))) {
                    Server(SERVERLOCALIP, PORT)
                } else {
                    Server(SERVERDNSADDRESS, PORT)
                }

                val device = Device(ipDevice)
                device.hisServer = server
                when(changeStatusTo) {
                    true -> device.turnOnDevice()
                    false -> device.turnOffDevice()
                    null -> device.upgradeStatus()
                }
                changeUi(device.status.toString())

            } catch (e: Exception) {
                changeUi("Server no Connection")
                e.printStackTrace()
            }
        }
    }

    private fun changeUi(text: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val btn = findViewById<Button>(R.id.button)
            btn.text = text.toUpperCase(Locale.getDefault())
        }
    }
}


/*-----------------------------------------------F-U-N-C-T-I-O-N-S------------------------------------------------------------------*/

fun myIp(): String? {
    try{
        val whatMyIp = URL("https://checkip.amazonaws.com")
        val input = BufferedReader(InputStreamReader(whatMyIp.openStream()))
        return input.readLine()
    } catch (e: Exception){
        e.printStackTrace()
    }

    return null
}


fun hostIp(hostName: String): String?{
    return try{
        val inetAddressObject = InetAddress.getByName(hostName)
        inetAddressObject.hostAddress

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun ipsAreSame(hostIp: String?, myIp: String?): Boolean {
    if(hostIp != null || myIp != null) {
        return myIp == hostIp
    }
    return false
}