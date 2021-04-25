package com.mtkreader.contracts

interface ReadingContract {

    interface View : BluetoothContract.View {
        fun onReadoutDone(readout: String)
        fun onByte(byte: Byte)

    }

    interface Presenter : BluetoothContract.Presenter {}
}
