package com.example.automatize.view.activity.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.automatize.R
import com.example.automatize.model.Device
import java.util.*

class MainAdapter(val context: Context): RecyclerView.Adapter<DeviceViewHolder>() {

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
            .inflate(R.layout.card_view_layout, parent, false) as GridLayout

        return DeviceViewHolder(listItemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {

        holder.deviceName.text = devicesList[position].name
        holder.deviceStatus.text = setDeviceAttributesButton(holder.image, devicesList[position].status)
        holder.cardView.setOnClickListener {
            val tmpDevice = instantiateTempDevice(devicesList[position])
            tmpDevice.status = if (tmpDevice.status == Device.COMMAND_OFF) Device.COMMAND_ON else Device.COMMAND_OFF
            deviceToCommand.postValue(tmpDevice)
        }

        holder.cardView.setOnLongClickListener {
            true
        }
    }

    private fun instantiateTempDevice(d: Device): Device {
        val device = Device(d.name, d.topicCommand, d.topicResponse, d.topicWill, d.type)
        device.status = d.status
        return device
    }

    private fun setDeviceAttributesButton(imageView: ImageView, status: String): String {
        return when {
            status.toUpperCase(Locale.ROOT) == Device.COMMAND_ON -> {
                changeImageNightModeCompat(imageView, toOn = true)
                context.getString(R.string.deviceOn)
            }
            status.toUpperCase(Locale.ROOT) ==  Device.COMMAND_OFF -> {
                changeImageNightModeCompat(imageView, toOn = false)
                context.getString(R.string.deviceOff)
            }
            else -> {
                status
            }
        }
    }

    private fun changeImageNightModeCompat(imageView: ImageView, toOn: Boolean) {
        // uimode Retorna Um Inteiro 1 Número A Mais Do Que As Constantes Definidas Em Configuration
        val currentMode = context.resources.configuration.uiMode
        if ( currentMode == Configuration.UI_MODE_NIGHT_YES+1 ) {
            if (toOn) {
                imageView.setImageResource(R.drawable.ic_baseline_emoji_objects_yellow_72)
            } else {
                imageView.setImageResource(R.drawable.ic_baseline_emoji_objects_72)
            }

        } else {
            if (toOn) {
                imageView.setImageResource(R.drawable.ic_baseline_emoji_objects_yellow_72)
            } else {
                imageView.setImageResource(R.drawable.ic_baseline_emoji_objects_black_72)
            }
        }
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }
}

class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
    val image: ImageView = itemView.findViewById(R.id.lamp)
    val cardView: CardView = itemView.findViewById(R.id.CV_carView)
    val deviceName: TextView = itemView.findViewById(R.id.deviceName)
    val deviceStatus: TextView = itemView.findViewById(R.id.deviceStatus)

}