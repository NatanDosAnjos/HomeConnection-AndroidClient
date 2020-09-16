package com.example.automatize.util

import android.content.Context
import android.os.CountDownTimer
import android.widget.Button
import android.widget.Toast
import java.util.*

class MyTimer(
    private val context: Context,
    private val button: Button,
    timeInFuture: Long = 0L,
    interval: Long = 1000,
    private val runWhenTimeFinished: Runnable? = null): CountDownTimer(timeInFuture, interval) {

    var isRunning: Boolean = false



    override fun onTick(millisUntilFinished: Long) {
        isRunning = true
        val timeText = "${getCorrectTimer(millisUntilFinished, true)}:${getCorrectTimer(millisUntilFinished)}"
        button.text = timeText
    }

    override fun onFinish() {
        isRunning = false
        Toast.makeText(context,"Time Finished", Toast.LENGTH_SHORT).show()

        runWhenTimeFinished?.run()
    }

    private fun getCorrectTimer(millisUntilFinished: Long, isMinute: Boolean = false):String {
        val constCalendar = if(isMinute) Calendar.MINUTE else Calendar.SECOND
        val calendar = Calendar.getInstance()
        val aux: String
        calendar.timeInMillis = millisUntilFinished

        aux = if(calendar.get(constCalendar) < 10) "0${calendar.get(constCalendar)}" else "${calendar.get(constCalendar)}"

        return aux
    }

}