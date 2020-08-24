package com.mtkreader.views.adapters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mtkreader.R
import kotlinx.android.synthetic.main.connected_device_item.view.*

class ConnectedDevicesRecyclerView(
    private val context: Context,
    private val layoutInflater: LayoutInflater
) :
    RecyclerView.Adapter<ConnectedDevicesRecyclerView.ConnectedDeviceViewHolder>() {

    interface OnItemClickListener {
        fun onClick(device: BluetoothDevice)
    }

    private val devices = mutableListOf<BluetoothDevice>()
    private var onClickListener: OnItemClickListener? = null

    fun addData(devices: Set<BluetoothDevice>) {
        this.devices.clear()
        this.devices.addAll(devices)
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        onClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectedDeviceViewHolder {
        val view = layoutInflater.inflate(R.layout.connected_device_item, parent, false)
        return ConnectedDeviceViewHolder(context, view, onClickListener)
    }

    override fun getItemCount(): Int = devices.size
    override fun onBindViewHolder(holder: ConnectedDeviceViewHolder, position: Int) {
        holder.addValuesOnHolder(devices[position])
    }


    class ConnectedDeviceViewHolder(
        private val context: Context,
        val view: View,
        private val onClickListener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(view) {

        fun addValuesOnHolder(device: BluetoothDevice) {
            with(device) {
                view.iv_device.setImageResource(provideTypeIcon(type))
                view.tv_device_name.text = name
                view.tv_device_adress.text =
                    String.format(context.resources.getString(R.string.square_bracket), address)

                view.setOnClickListener {
                    //view.setBackgroundColor(context.resources.getColor(R.color.colorAccent))
                    onClickListener?.onClick(this)
                }

            }

        }

        private fun provideTypeIcon(type: Int): Int {
            return when (type) {
                BluetoothProfile.HEADSET -> R.drawable.ic_music_type
                else -> R.drawable.ic_unknown_type
            }
        }
    }

}