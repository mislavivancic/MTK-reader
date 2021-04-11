package com.mtkreader.views.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mtkreader.R
import kotlinx.android.synthetic.main.connected_device_item.view.*

enum class DeviceOperation {
    TIME_READ, TIME_SET, PARAM_READ
}

class ConnectedDevicesRecyclerView(private val layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<ConnectedDevicesRecyclerView.ConnectedDeviceViewHolder>() {

    private var clickedPosition = -1

    private val expandedSet = mutableSetOf<BluetoothDevice>()

    interface OnItemClickListener {
        fun onClick(device: BluetoothDevice, deviceOperation: DeviceOperation)
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
        return ConnectedDeviceViewHolder(view, onClickListener)
    }

    override fun getItemCount(): Int = devices.size
    override fun onBindViewHolder(holder: ConnectedDeviceViewHolder, position: Int) {
        holder.addValuesOnHolder(
            devices[position],
            expandedSet.contains(devices[position])
        )
        holder.itemView.cl_device_info.setOnClickListener {
            if (expandedSet.contains(devices[position]))
                expandedSet.remove(devices[position])
            else
                expandedSet.add(devices[position])
            clickedPosition = position
            notifyItemChanged(position)
        }
        holder.itemView.btn_options.setOnClickListener {
            if (expandedSet.contains(devices[position]))
                expandedSet.remove(devices[position])
            else
                expandedSet.add(devices[position])
            clickedPosition = position
            notifyItemChanged(position)
        }
    }


    class ConnectedDeviceViewHolder(
        private val view: View,
        private val onClickListener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(view) {

        fun addValuesOnHolder(device: BluetoothDevice, shouldExpand: Boolean) {
            with(device) {
                view.tv_device_name.text = name
                view.tv_device_adress.text =
                    String.format(view.resources.getString(R.string.square_bracket), address)

                view.btn_time_read.setOnClickListener {
                    onClickListener?.onClick(
                        this,
                        DeviceOperation.TIME_READ
                    )
                }

                view.btn_time_set.setOnClickListener {
                    onClickListener?.onClick(
                        this,
                        DeviceOperation.TIME_SET
                    )
                }
                view.btn_param_read.setOnClickListener {
                    onClickListener?.onClick(
                        this,
                        DeviceOperation.PARAM_READ
                    )
                }
                if (shouldExpand) {
                    view.btn_options.animate().setDuration(200).rotation(180f)
                    view.options_container.visibility = View.VISIBLE
                } else {
                    view.btn_options.animate().setDuration(200).rotation(0f)
                    view.options_container.visibility = View.GONE
                }

            }

        }
    }

}