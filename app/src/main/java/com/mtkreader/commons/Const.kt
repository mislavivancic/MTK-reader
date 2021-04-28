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
        const val DEVICE_OPERATION = "DEVICE_OPERATION"
        const val DATA_EXTRA = "DATA_EXTRA"
    }

    object SharedPrefKeys {
        const val READ_DATA_KEY = "READ_DATA_KEY"
        const val LAST_FILE_READ_KEY = "LAST_FILE_READ_KEY"
    }

    object PermissionCode {
        const val GRANTED = 0
        const val DENIED = -1
    }

    object ShowCase {
        const val PROGRAM_TIME = "PROGRAM_TIME"
        const val ERROR_TIME = "ERROR_TIME"
    }

    object Error {
        const val BT_NOT_SUPPORTED = "Bluetooth is not supported on this device!"
        const val BT_REQUIRED = "Bluetooth is required!"
    }

    object BluetoothConstants {
        const val UUID = "00001101-0000-1000-8000-00805f9b34fb"

        const val INIT_COMMUNICATION_INTERVAL: Long = 2000
        const val TIME_QUERY_INTERVAL: Long = 2000
        const val CONNECTION_TIMEOUT: Long = 10000

        const val FIRST_LINE_TOKEN_FIRST = 13.toByte().toChar()
        const val FIRST_LINE_TOKEN_SECOND = 10.toByte().toChar()
        const val SECOND_LINE_TOKEN = 6.toByte().toChar()
        const val SECOND_LINE_TOKEN_OTHER = 127.toByte().toChar()
    }

    object Logging {
        const val SENT = "MESSAGE SENT"
        const val RECEIVED = "MESSAGE RECEIVED"
        const val PACK = "PACK"
        const val MONITOR = "MONITOR"
    }

    object DeviceConstants {
        const val NAME = "HELM"

        const val FIRST_INIT = "2F3F210D0A"
        const val SECOND_INIT = "063034360D0A"
        const val WRITE_PARAMS_SECOND_INIT = "063034310D0A"
        const val WRITE_PARAMS_THIRD_INIT = "0142300371"
        const val GET_TIME = "0147740330"
        const val RESET = "01810382"
        const val ACK = "01550356"
    }
    object PARAMSRC {
       const val FILE = 0
       const val DEVICE = 1
       const val NEW = 2
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
        const val EMT_REL_X = 0x1000

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
        const val TIP_PSB = 8

        const val SNO_RTC= 0x20

        const val SNO_REL1= 0x01
        const val SNO_REL2= 0x02
        const val SNO_REL3= 0x03
        const val SNO_REL4= 0x04

        const val SNO_PRIJEM= 0x10
        const val SNO_TLG=   0x80




        const val LEARN_DISEB_MASK = 0x80
        const val LEARN_LGHTS_MASK = 0x40
        const val LEARN_7DAYS_MASK = 0x10

        const val LEARN_R_ON_MASK   =  0x40
        const val LEARN_R_OFF_MASK  =  0x20
//---------------------------------PRIJEM EVENT---------------------------------------------
        const val PRIJ_EV_PON     =  0x01
        const val PRIJ_EV_TLG	  =    0x02				//novi tlg OK
        const val PRIJ_EV_STIMP   =  0x04				//start imp OK
        const val PRIJ_EV_EMTLG   =  0x08
        const val PRIJ_EV_SINH    =  0x10
        const val PRIJ_EV_MYTLG   =  0x20				//teleg upisan u prijemnik
        const val PRIJ_EV_RTCST   =  0x40       //RTC ST ili  OF
        const val PRIJ_EV_RTCOF   =  0x80
//---------------------------RELEJ EVENTS--------------------
        const val REL_EV_TL_ON      =   0x20
        const val REL_EV_TL_OFF     =   0x10
        const val REL_EV_TL_LDIS	=     0x80				//learn dis
        const val REL_EV_TL_LINTR	=     0x40				//learn break
        const val REL_EV_TL_PROEN   =   0x01				//prog enable
        const val REL_EV_TL_PRODI   =   0x02				//prog disable
//-----------------------------------------RELEJ STATUS-------------------------
        const val REL_LEARN_EN 	=0x80
        const val REL_LEARN_INTR=	0x40
        const val REL_PROG_UNLOCK= 0x01
        const val REL_TA_STATE =	0x20
        const val REL_TIMPR_UNLOCK =0x02
        const val REL_EM_STATE = 0x8

//-----------------------------------------AKT FN-------------------------
        const val AKT_FN_NOF=		0x0
        const val AKT_FN_DLY=		0x01
        const val AKT_FN_WIPER=	0x02
        const val AKT_FN_VRETCI=	0x04	//retrigg cik funkcije
        const val AKT_FN_VRES=		0x08
        const val AKT_FN_VCI=		0x09
        const val AKT_FN_VCI2=		0x0A
        const val AKT_FN_PONW=		0x0C		//wait on Pon;
        const val AKT_SET_ON=		0x80
        const val AKT_SET_OFF=		0x40
        const val AKT_POS_ON=		0x20
        const val AKT_POS_OFF=		0x10
        const val AKT_EMERG_TLG= 0x100




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
        val adressesC = arrayOf(
            "C080",
            "9080",
            "9180",
            "9280",
            "9380",
            "9480",
            "9580",
            "9680",
            "8080",
            "8180",
            "5080",
            "5180",
            "5280",
            "5380",
            "9880",
            "9980",
            "9A80",
            "9B80",
            "9C80",
            "0380",
            "0280",
            "8280"
        )

        val CTipPrij = arrayOf(
            "-S ",
            "-SN ",
            "-SPA ",
            "-PAS ",
            "-PA ",
            "-PASN ",
            "-SPN ",
            "-PS ",
            "-PSB "
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