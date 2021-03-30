package com.example.automatize.view.activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.automatize.R
import com.example.automatize.model.Device

class MainAdapter(): RecyclerView.Adapter<DeviceViewHolder>() {

    private var devicesList = mutableListOf<Device>()
    private var deviceToCommand = MutableLiveData<Device>()

    fun getDeviceToCommand(): LiveData<Device> {
        return deviceToCommand
    }

    fun updateList(list: MutableList<Device>) {
        devicesList = list
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val listItemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false) as LinearLayout

        return DeviceViewHolder(listItemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.switch.text = devicesList[position].name
        holder.switch.isChecked = devicesList[position].status == Device.COMMAND_ON
        holder.switch.setOnClickListener {
            devicesList[position].status = if (holder.switch.isChecked) Device.COMMAND_ON else Device.COMMAND_OFF
            deviceToCommand.postValue(devicesList[position])
        }

        holder.switch.setOnLongClickListener {
            true
        }
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }
}

class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
    val switch: SwitchCompat = itemView.findViewById(R.id.switchBtn)
}


