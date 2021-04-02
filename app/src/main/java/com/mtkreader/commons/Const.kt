package com.mtkreader.commons

object Const {
    object RequestCode {
        const val REQUEST_ENABLE_BT = 1
    }


    object Tokens {
        const val PARAM_READ_END_TOKEN = 33.toByte().toChar()
        const val GET_TIME_END_TOKEN = ')'.toByte().toChar()
    }

    object Extras {
        const val SOCKET_EXTRA = "SOCKET_EXTRA"
        const val DEVICE_EXTRA = "DEVICE_EXTRA"
        const val DATA_EXTRA = "DATA_EXTRA"
    }

    object SharedPrefKeys {
        const val READ_DATA_KEY = "READ_DATA_KEY"
    }

    object PermissionCode {
        const val GRANTED = 0
        const val DENIED = -1
    }

    object Error {
        const val BT_NOT_SUPPORTED = "Bluetooth is not supported on this device!"
        const val BT_REQUIRED = "Bluetooth is required!"
    }

    object BluetoothConstants {
        const val UUID = "00001101-0000-1000-8000-00805f9b34fb"
    }

    object Logging {
        const val SENT = "MESSAGE SENT"
        const val RECEIVED = "MESSAGE RECEIVED"
    }

    object DeviceConstants {
        const val NAME = "HELM"

        const val FIRST_INIT = "2F3F210D0A"
        const val SECOND_INIT = "063034360D0A"
        const val GET_TIME = "0147740330"
        const val RESET = "01810382"
        const val ACK = "015503560D0A"
    }

    object Data {
        const val TIM_LOSS_RTC_POS = 0x02
        const val SINH_REL_POS_MASK = 0x40

        const val WIPER_DISEB_MASK = 0x80
        const val WIPPER_RETRIG_MASK = 0x01
        const val WIPER_ON_MASK = 0x40
        const val WIPER_OFF_MASK = 0x20
        const val LOOP_DISEB_MASK = 0x08
        const val PON_DISLRN_I_W_MASK = 0x80
        const val PON_LPERIOD_DIS_MASK = 0x80
        const val TLGA_ON_DISLRN = 0x80
        const val LEARN_7DAYS_MASK = 0x10
        const val LEARN_R_ON_MASK = 0x40
        const val LEARN_R_OFF_MASK = 0x20


        const val SNE_POFF = 0x0001
        const val SNE_PON = 0x0002
        const val SNE_RTC_ST = 0x0004
        const val SNE_RTC_OF = 0x0008
        const val SNE_SHT = 0x0010 //

        const val SNE_SHD = 0x0020 //

        const val SNE_RTC_BL = 0x0040 //

        const val SNE_RTC_OOK = 0x0080
        const val SNE_LSINH = 0x0100 //

        const val SNE_WPAROK = 0x0400 //

        const val SNE_WPARERR = 0x0800 //


        const val OPT_LOG_MYTLG = 0x4000
        const val OPT_LOG_TLG = 0x8000
        const val OPT_LOG_REPTLG = 0x2000

        const val REL_ON = 0x0001
        const val REL_OFF = 0x0002
        const val REL_PROBLOCK = 0x0004
        const val REL_PROUNBLOCK = 0x0008
        const val REL_WIP_S = 0x0010
        const val REL_WIP_R = 0x0020
        const val REL_TA_S = 0x0040
        const val REL_TA_R = 0x0080
        const val PRO_REL_X = 0x0100
        const val TEL_REL_X = 0x0200
        const val CLP_REL_X = 0x0400
        const val PON_REL_X = 0x0800

        const val PRO_REL_ON = 0x0101
        const val PRO_REL_OFF = 0x0102

        const val TIP_S = 0
        const val TIP_SN = 1
        const val TIP_SPA = 2
        const val TIP_PAS = 3
        const val TIP_PA = 4
        const val TIP_PASN = 5
        const val TIP_SPN = 6
        const val TIP_PS = 7
        val bVtmask = byteArrayOf(
            0x80.toByte(),
            0x40.toByte(),
            0x20.toByte(),
            0x10.toByte(),
            0x08.toByte(),
            0x04.toByte(),
            0x02.toByte(),
            0x01.toByte()
        )

        val CTipPrij = arrayOf(
            "-S ",
            "-SN ",
            "-SPA ",
            "-PAS ",
            "-PA ",
            "-PASN ",
            "-SPN ",
            "-PS "
        )
        val COMPLETE = 2.toByte()
        val SOH = 0x01.toByte()
        val STX = 0x02.toByte()
        val ETX = 0x03.toByte()
        val EOT = 0x04.toByte()
        val ACK = 0x06.toByte()
        val NAK = 0x15.toByte()

        val TIME_FORMAT = "G0(%02X%02X%02X%02X)"
        val TIME_DATE_FORMAT = "G0(%02X%02X%02X%02X%02X%02X%02X)"
    }


}