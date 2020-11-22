package com.example.automatize.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.automatize.R
import com.example.automatize.model.Device
import com.example.automatize.model.ServerToConnect

class MainAdapter(private val devicesList: List<Device>, private val server: ServerToConnect) : RecyclerView.Adapter<MainAdapter.DeviceViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val listItemView = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false) as LinearLayout

        return DeviceViewHolder(listItemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.switch.text = devicesList[position].name
        holder.switch.isChecked = devicesList[position].status == "h"
        holder.switch.setOnClickListener {
            val command = if(holder.switch.isChecked) "h" else "l"
            server.clientMqtt.publish(devicesList[position].topicCommand, command.toByteArray(), 2, true)
        }

        holder.switch.setOnLongClickListener {
            true
        }

    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        val switch: SwitchCompat = itemView.findViewById(R.id.switchBtn)
    }
}

