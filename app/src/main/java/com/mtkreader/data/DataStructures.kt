package com.mtkreader.data

import com.mtkreader.commons.Const
import com.mtkreader.data.reading.*

class DataStructures {
    var globalIndex = 0
    var m_paramSrc= Const.PARAMSRC.NEW
    var mTip = 0
    var mHardwareVersion = 0
    var mSoftwareVersion = 0
    var mParFiltera = StrParFil()
    val mParFilteraCF = StrParFilVer9()
    var mBrojRast = 0
    var mUtfPosto = 0.0
    val UTFREFP = 0.9
    var mBrUpKalendara: Byte = 0
    val mCfg = CfgParHwsw()
    var mFileComment = ""
    val mOprij = Oprij()
    val mOp50rij = Oprij50()
    val mRealloc = Array(4) { Rreallc() }
    val mTelegSync = Array(13) { Telegram() }
    val m_TlgFnD = Array(8) { Telegram() }
    val mPProgR1 = Array(16) { Opprog() }
    val mPProgR2 = Array(16) { Opprog() }
    val mPProgR3 = Array(16) { Opprog() }
    val mPProgR4 = Array(16) { Opprog() }
    var mPBuff = ByteArray(256)
    var mPraznici = PrazniciStr()
    var mWipersRx = Array(4) { Wiper() }
    var mPonPoffRx = Array(4) { PonPoffStr() }
    var mTelegAbsenceRx = Array(4) { TlgAbstr() }
    var mLearningRx = Array(4) { StrLoadMng() }
    var mRelInterlock = Array(8) { IntrlockStr() }
    var mKalendar = Array(72) { StKalend() }
    var mInitRelSetProg = InitRelSetting()
    var mUkls = Ukls()
    var mCFileParData = RecFilParStr()  //upisano u file;
    var m_cLastParData = REC_PAR_STR() //proèitano iz ureðaja
    var m_cNewParData = REC_PAR_STR()  //upisano u ureðaj;
    var m_dwDeviceSerNr=0
    var m_LoopPar = Array(4) { LOOPTIMSTR() }

}