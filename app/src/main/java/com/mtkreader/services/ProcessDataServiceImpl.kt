package com.mtkreader.services

import android.content.Context
import android.util.Log
import com.mtkreader.commons.Const
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.reading.*
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.DataUtils.HtoB
import com.mtkreader.utils.DataUtils.strCopyHexToBuf
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.experimental.or

class ProcessDataServiceImpl : DisplayDataContract.ProcessService, KoinComponent {

    val context: Context by inject()
    private val data: DataStructures = DataStructures()
    // move this elsewhere



    private var globalIndex = 0

    private val mline = ByteArray(256)
    private var m_Dateerr = 0
    private var m_cntxx = 1


    private val UTFREFP = 0.9

    private var m_SWVerPri = 0
    private var m_HWVerPri = 0

    private fun getStringArray(resId: Int, index: Int): String {
        return context.resources.getStringArray(resId)[index]
    }

    private fun getString(resId: Int): String {
        return context.resources.getString(resId)
    }

    override fun processData(header: ByteArray, dataL: ByteArray): DataStructures {

        getVersions(header)


        mline[0] = 0

        while (GetMsgLine(dataL)) {
            if (mline[0] != Const.Tokens.PARAM_READ_END_TOKEN.toByte()) {
                if (mline[0] == '!'.toByte()) break
                GetLineDat()
            }
            else
                break
        }
        data.m_paramSrc=Const.PARAMSRC.DEVICE

        return data
    }


    private fun GetMsgLine(data: ByteArray): Boolean {
        var i = 0
        while (m_cntxx < data.size) {
            if (data[m_cntxx] == 0x0D.toByte()) {
                mline[i++] = data[m_cntxx++]

                if (data[m_cntxx] == 0x0A.toByte()) {
                    mline[i++] = data[m_cntxx++]
                    mline[i] = 0
                    return true
                } else {
                    mline[i++] = 0
                    m_Dateerr++
                    return false
                }
            }
            mline[i++] = data[m_cntxx++]
        }
        m_Dateerr++
        return false
    }

    private fun GetLineDat() {
        var i = 0
        val m_gaddr = Mgaddr(0)
        var bb: Char?

        while (i < 5) {
            if (mline[i] == '('.toByte())
                break
            bb = HtoB(mline[i++].toChar())
            if (bb != null) {
                m_gaddr.i = m_gaddr.i shl 4
                m_gaddr.i = m_gaddr.i or bb.toInt()
            } else
                m_Dateerr++
        }
        m_gaddr.update()


        if (mline[i] != '('.toByte()) // TODO 	if(!(m_Line[i++]=='('))m_Daterr++;

            m_Dateerr++

        i = 5
        if (mline[0] == 63.toByte()) i++ //HACK TODO
        val dbuf = ByteArray(128)


        for (j in 0..127) {//INDEX 128>od velicine dbuf pa bi trebalo bit 127??
            var k = 2
            while (k != 0) {
                k--
                if (mline[i] == ')'.toByte())
                    break
                bb = HtoB(mline[i++].toChar())
                if (bb != null) {
                    dbuf[j] = (dbuf[j].toInt() shl 4).toByte()
                    dbuf[j] = dbuf[j] or bb.toByte()
                } else
                    m_Dateerr++
            }
            if (mline[i] == ')'.toByte())
                break
        }

        unpackDatV9(dbuf, m_gaddr)
    }

    private fun unpackDatV9(dbuf: ByteArray, mgaddr: Mgaddr) {
        globalIndex = 0 //TODO  mislim da se samo tu treba resetirat na nulu
        Log.i(Const.Logging.PACK, "group: ${mgaddr.group} obj ${mgaddr.objectt}")

        when (mgaddr.group) {
            0 -> {
                when (mgaddr.objectt) {
                    1 -> mgaddr.objectt //var pFrameWnd->m_opPrij.VAdrPrij=pFrameWnd->SetOprel4I(); //TODO implementirati
                    2 -> getcLastParData(dbuf) //TODO napravit metodu
                    3 -> data.mRelInterlock = getRelInterLock(dbuf).toTypedArray() //TODO chekirat
                    4 -> getDeviceSerNr(dbuf)
                }
            }
            1 -> getTparPar(dbuf, data.mPProgR1[mgaddr.objectt])
            2 -> getTparPar(dbuf, data.mPProgR2[mgaddr.objectt])
            3 -> getTparPar(dbuf, data.mPProgR3[mgaddr.objectt])
            4 -> getTparPar(dbuf, data.mPProgR4[mgaddr.objectt])

            0xC -> getFriRPar(dbuf, data.mParFilteraCF, data.mParFiltera)

            7 -> {
                // PRAZNO ZA PS i PSB
            }
            5 -> {
                when (mgaddr.objectt) {
                    0 -> data.mWipersRx = getWipers(dbuf).toTypedArray()
                    1 -> data.mPonPoffRx = getPonPoffRDat(dbuf).toTypedArray()
                    2 -> data.mTelegAbsenceRx = getTlgAbsenceDat(dbuf).toTypedArray()
                    3 -> data.mLearningRx = getLearningDat(dbuf).toTypedArray()
                }
            }
            6 -> GetLoopTim(dbuf) //TODO implementirati

            8 -> getOprijParV9(mgaddr, dbuf, data.mOp50rij, data.mOprij, data.mRealloc.toMutableList())
            9 -> getTlg50Par(mgaddr, dbuf, data.mOp50rij, data.mTelegSync.toMutableList(), data.m_TlgFnD.toList())
        }

    }

    private fun getDeviceSerNr(dbuf: ByteArray) {

        data.m_dwDeviceSerNr = setOprel4I(dbuf)

        //pFrameWnd->m_dwDeviceSerNr = pFrameWnd->SetOprel4I();
//
        //CString str;
        //str.Format(_T("\n\r"+CMsg(IDSI_SERIALNUM)+"  %d  "), pFrameWnd->m_dwDeviceSerNr);
        //pFrameWnd->ShowData(str);

    }

    private fun GetLoopTim(dbuf: ByteArray) {

        //typedef struct {
        //    byte State;		// ne koristi se
        //    byte LastCmd;		//0 nepoznata  1 ON 2 OFF
        //    WTONOFF  Tpar;

        //} LOOPTIMSTR;

        //typedef struct{
        //    WORD ton;
        //    WORD toff;
        //}

        for (uRel in 0..3) {
            data.m_LoopPar[uRel].State = dbuf[globalIndex++]
            data.m_LoopPar[uRel].LastCmd = dbuf[globalIndex++]
            data.m_LoopPar[uRel].Tpar.ton = setOprelI(dbuf).toShort()
            data.m_LoopPar[uRel].Tpar.toff = setOprelI(dbuf).toShort()
        }

        // CString str = _T("Loop:\n\r");
        // for (UINT uRel = 0; uRel < 4; uRel++)
        // {
        //     m_LoopPar[uRel].State = *m_pbuf++;
        //     m_LoopPar[uRel].LastCmd = *m_pbuf++;
        //     m_LoopPar[uRel].Tpar.ton = SetOprelI();
        //     m_LoopPar[uRel].Tpar.toff = SetOprelI();
//
        //     CString strllt ;
//
        //     if((m_LoopPar[uRel].Tpar.ton & 0x8000 ) || (m_LoopPar[uRel].Tpar.toff & 0x8000))
        //     strllt.Format(_T("%02X -:- -:-\n\r "), m_LoopPar[uRel].LastCmd);
        //     else
        //     strllt.Format(_T("%02X %02d:%02d  %02d:%02d\n\r "), m_LoopPar[uRel].LastCmd, m_LoopPar[uRel].Tpar.ton/60, m_LoopPar[uRel].Tpar.ton % 60, m_LoopPar[uRel].Tpar.toff / 60, m_LoopPar[uRel].Tpar.toff % 60);
//
        //     str += strllt;
        // }
        // //ShowData(str);
    }

    private fun getcLastParData(dbuf: ByteArray) {


        //#define PARIDFILE_SIZE 18
        //#define PARID_SIZE 8
        //typedef struct {
        //    byte	DataTime[6];
        //    byte  CreateSite[PARID_SIZE];
        //    byte  IDCreate[PARID_SIZE];
        //    byte  ReParaSite[PARID_SIZE];
        //    byte  IDRePara[PARID_SIZE];
//
        //    byte  IDFile[PARIDFILE_SIZE];
        //}REC_PAR_STR;
//
        //typedef struct {
        //    byte  CreateSite[PARID_SIZE];
        //    byte  IDCreate[PARID_SIZE];
        //    byte  IDFile[PARIDFILE_SIZE];
        //}REC_FILPAR_STR;


        for (i in 0 until REC_PAR_STR.DataTime_SIZE) data.m_cLastParData.DataTime[i] = dbuf[globalIndex++]
        for (i in 0 until REC_PAR_STR.PARID_SIZE) data.m_cLastParData.CreateSite[i] = dbuf[globalIndex++]
        for (i in 0 until REC_PAR_STR.PARID_SIZE) data.m_cLastParData.IDCreate[i] = dbuf[globalIndex++]
        for (i in 0 until REC_PAR_STR.PARID_SIZE) data.m_cLastParData.ReParaSite[i] = dbuf[globalIndex++]
        for (i in 0 until REC_PAR_STR.PARID_SIZE) data.m_cLastParData.IDRePara[i] = dbuf[globalIndex++]
        for (i in 0 until REC_PAR_STR.PARIDFILE_SIZE) data.m_cLastParData.IDFile[i] = dbuf[globalIndex++]

        var s = data.m_cLastParData.toString()


        // REC_PAR_STR *pLastPa = &(pFrameWnd->m_cLastParData);
        // memcpy(pLastPa, pFrameWnd->m_pbuf, sizeof(REC_PAR_STR));
        // CString str;
//
        // byte IDCreate[PARID_SIZE+1];		memset(&IDCreate	,0,PARID_SIZE+1);		memcpy(&IDCreate,	pLastPa->IDCreate,	PARID_SIZE );
        // byte CreateSite[PARID_SIZE + 1];	memset(&CreateSite, 0, PARID_SIZE + 1);		memcpy(&CreateSite, pLastPa->CreateSite, PARID_SIZE);
        // byte IDRePara[PARID_SIZE + 1];		memset(&IDRePara, 0, PARID_SIZE + 1);		memcpy(&IDRePara,	pLastPa->IDRePara,	PARID_SIZE);
        // byte ReParaSite[PARID_SIZE + 1];	memset(&ReParaSite, 0, PARID_SIZE + 1);		memcpy(&ReParaSite, pLastPa->ReParaSite, PARID_SIZE);
        // byte IDFile[PARIDFILE_SIZE + 1];	memset(&IDFile, 0, PARIDFILE_SIZE + 1);		memcpy(&IDFile,		pLastPa->IDFile,	 PARIDFILE_SIZE);
//
        // str.Format(_T("\n\r"+CMsg(IDSI_LASTREPARAMTIME)+" %02X-%02X-%02X %02X:%02X\n\r"+CMsg(IDSI_CREATED_UID)+" %s "+CMsg(IDSI_CREATED_PCUID)+" |%s|\n\r"+CMsg(IDSI_REPARM_UID)+" %s "+CMsg(IDSI_REPARM_PCUID)+" |%s|\n\r"+CMsg(IDSI_PARM_FILE)+"%s.mtk \n\r"), pLastPa->DataTime[3], pLastPa->DataTime[4],
        // pLastPa->DataTime[5], pLastPa->DataTime[2], pLastPa->DataTime[1], IDCreate, CreateSite, IDRePara, ReParaSite, IDFile );
        // pFrameWnd->ShowData(str);


    }





    private fun getFriRPar(
        dbuf: ByteArray,
        mParFilteraCf: StrParFilVer9,
        mParFiltera: StrParFil
    ) {
        if (m_SWVerPri >= 90) {
            getFriRParVer9(dbuf, mParFilteraCf)
        } else {
            mParFiltera.apply {
                NYM1 = dbuf[globalIndex++]
                NYM2 = dbuf[globalIndex++]
                YM_B1 = setOprelI(dbuf)
                YM_B2 = setOprelI(dbuf)
                UTHMIN = setOprelI(dbuf)
                UTLMAX = setOprelI(dbuf)
                PERIOD = setOprelI(dbuf)
                FORMAT = dbuf[globalIndex++].toInt()
                BROJ = dbuf[globalIndex++].toInt()
                data.mBrojRast = setOprelI(dbuf)
            }
        }
    }

    private fun getFriRParVer9(dbuf: ByteArray, mParFilteraCf: StrParFilVer9) {
        mParFilteraCf.apply {
            NYM1 = dbuf[globalIndex++]
            NYM2 = dbuf[globalIndex++]
            K_V = setOprelI(dbuf)
            REZ = setOprelI(dbuf)
            UTHMIN = setOprelI(dbuf)
            UTLMAX = setOprelI(dbuf)
            PERIOD = setOprelI(dbuf)
            FORMAT = setOprelI(dbuf)
            BROJ = dbuf[globalIndex++].toInt()
            globalIndex++
            data.mBrojRast = setOprelI(dbuf)
            setUfPosto(this)
        }
    }

    private fun setUfPosto(mParFilteraCf: StrParFilVer9) {
        val broj = mParFilteraCf.BROJ
        if (broj >= 0) {
            var KvUt = mParFilteraCf.K_V
            if (KvUt == 0)
                KvUt = DataUtils.getTbParFilteraVer9()[broj].K_V

            val utth = mParFilteraCf.UTHMIN.toDouble()
            var uthMin = utth / KvUt.toDouble()

            uthMin *= 1.002
            var utlMax = mParFilteraCf.UTLMAX.toDouble() / KvUt.toDouble()
            utlMax *= 1.002

            var uthMinRef = 0.0
            val ith = getFrUTHDefVer9(broj)
            if (ith != 0) {
                uthMinRef = ith.toDouble()
                data.mUtfPosto = (utth * UTFREFP) / uthMinRef
            } else
                data.mUtfPosto = 0.0
        }
    }

    private fun getFrUTHDefVer9(broj: Int): Int {
        var uth = 0
        if (broj >= 0)
            uth = DataUtils.getTbParFilteraVer9()[broj].UTHMIN

        return uth
    }

    private fun getTlg50Par(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mTelegSync: List<Telegram>,
        mTlgFnd: List<Telegram>
    ) {
        if (m_SWVerPri >= 96) {
            getTlgID50ParV96(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnd)
        }
    }

    private fun getTlgID50ParV96(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mTelegSync: List<Telegram>,
        mTlgFnd: List<Telegram>
    ) {
        when (mgaddr.objectt) {
            0 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel1)  //TODO iskoristit storeDataTlgFn
            1 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel2)
            2 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel3)
            3 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel4)


            4 -> {
                storeDataTlgFn(dbuf, mOp50Prij.tlg[0].tel1)
                storeDataTlgFn(dbuf, mOp50Prij.tlg[1].tel1)
                storeDataTlgFn(dbuf, mOp50Prij.tlg[2].tel1)
            }
            5 -> {
                storeDataTlgFn(dbuf, mOp50Prij.tlg[3].tel1)
                storeDataTlgFn(dbuf, mOp50Prij.tlg[4].tel1)
            }
            6 -> {
                storeDataTlgFn(dbuf, mOp50Prij.tlg[5].tel1)
                storeDataTlgFn(dbuf, mOp50Prij.tlg[6].tel1)
                storeDataTlgFn(dbuf, mOp50Prij.tlg[7].tel1)
            }

            7 -> {
                storeDataTlgFn(dbuf, mOp50Prij.TlgVerAdr2.TlgAdr) //??

            }

            8 -> {
                storeDataTlgFn(dbuf, mTelegSync[0])
                storeDataTlgFn(dbuf, mTelegSync[1])
            }
            9 -> {
                storeDataTlgFn(dbuf, mTelegSync[2])
                storeDataTlgFn(dbuf, mTelegSync[3])
                storeDataTlgFn(dbuf, mTelegSync[4])
            }
            0x0A -> {
                storeDataTlgFn(dbuf, mTelegSync[5])
                storeDataTlgFn(dbuf, mTelegSync[6])
                storeDataTlgFn(dbuf, mTelegSync[7])
            }
            0x0B -> {
                storeDataTlgFn(dbuf, mTelegSync[8])
                storeDataTlgFn(dbuf, mTelegSync[9])
            }
            0x0C -> {
                storeDataTlgFn(dbuf, mTelegSync[10])
                storeDataTlgFn(dbuf, mTelegSync[11])
                storeDataTlgFn(dbuf, mTelegSync[12])
            }
        }
    }

    private fun storeDataTlgFn(dbuf: ByteArray, fn: Telegram) {
        val byteBuffer = mutableListOf<Byte>()
        for (i in 0..6) {
            byteBuffer.add(dbuf[globalIndex++])
        }
        fn.Cmd.AktiImp = byteBuffer.toByteArray()
        byteBuffer.clear()
        fn.Cmd.BrAkImp = dbuf[globalIndex++]

        for (i in 0..6)
            byteBuffer.add(dbuf[globalIndex++])
        fn.Cmd.NeutImp = byteBuffer.toByteArray()
        byteBuffer.clear()
        fn.Cmd.Fn = dbuf[globalIndex++]


        //TODO removed ID
        //val temp: Int = dbuf[globalIndex++].toInt()
        //var tempUp: Int = dbuf[globalIndex++].toInt()
        //tempUp = tempUp shl 8
        //fn.ID = temp or tempUp
    }

    private fun storeDataTlgRel(dbuf: ByteArray, tlgRel: Telegrel) {
        val byteBuffer = mutableListOf<Byte>()
        for (i in 0..6)
            byteBuffer.add(dbuf[globalIndex++])
        tlgRel.Uk.AktiImp = byteBuffer.toByteArray()
        byteBuffer.clear()

        tlgRel.Uk.BrAkImp = dbuf[globalIndex++]

        for (i in 0..6)
            byteBuffer.add(dbuf[globalIndex++])

        tlgRel.Uk.NeutImp = byteBuffer.toByteArray()
        byteBuffer.clear()

        tlgRel.Uk.Fn = dbuf[globalIndex++]


        for (i in 0..6)
            byteBuffer.add(dbuf[globalIndex++])
        tlgRel.Isk.AktiImp = byteBuffer.toByteArray()
        byteBuffer.clear()

        tlgRel.Isk.BrAkImp = dbuf[globalIndex++]

        for (i in 0..6)
            byteBuffer.add(dbuf[globalIndex++])
        tlgRel.Isk.NeutImp = byteBuffer.toByteArray()
        byteBuffer.clear()

        tlgRel.Isk.Fn = dbuf[globalIndex++]
        //TODO removed ID
        //val temp: Int = dbuf[globalIndex++].toInt()
        //var tempUp: Int = dbuf[globalIndex++].toInt()
        //tempUp = tempUp shl 8
//
        //tlgRel.ID = temp or tempUp
    }


    private fun getOprijParV9(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mOpPrij: Oprij,
        mReallocs: MutableList<Rreallc>
    ) {
        when (mgaddr.objectt) {
            0 -> if (m_SWVerPri >= 96)
                getKlDatVer96(dbuf, mOp50Prij, mOpPrij, mReallocs)

            1 -> if (m_SWVerPri >= 96)
                getKl2VerDatVer96(dbuf, mOp50Prij, mOpPrij)

            2 -> getDaljPar(dbuf, mOpPrij)
        }
    }

    private fun getDaljPar(dbuf: ByteArray, mOpPrij: Oprij) {
        mOpPrij.VOpRe.VakProR1 = setOprelI(dbuf)
        mOpPrij.VOpRe.VakProR2 = setOprelI(dbuf)
        mOpPrij.VOpRe.VakProR3 = setOprelI(dbuf)
        mOpPrij.VOpRe.VakProR4 = setOprelI(dbuf)

        if (m_SWVerPri < 98) mOpPrij.VOpRe.StaPrij=dbuf[globalIndex++]
        if(m_SWVerPri < 95)
        {
            mOpPrij.ParFlags=dbuf[globalIndex++]
            mOpPrij.StaR1PwON_OFF=dbuf[globalIndex++]
            mOpPrij.StaR2PwON_OFF=dbuf[globalIndex++]
            mOpPrij.StaR3PwON_OFF=dbuf[globalIndex++]
            mOpPrij.StaR4PwON_OFF=dbuf[globalIndex++]
        }
        else	{

            mOpPrij.ParFlags=0;

            mOpPrij.StaR1PwON_OFF=0;
            mOpPrij.StaR2PwON_OFF=0;
            mOpPrij.StaR3PwON_OFF=0;
            mOpPrij.StaR4PwON_OFF=0;
        }


    }

    private fun getKl2VerDatVer96(dbuf: ByteArray, mOp50Prij: Oprij50, mOpPrij: Oprij) {
        var inCik = false

        if ((data.mCfg.cID == 100) || (data.mCfg.cID == 120))
            inCik = true

        if (inCik)
            mOpPrij.VDuzAdr = dbuf[globalIndex++]

        mOpPrij.PolUKRe = dbuf[globalIndex++]

        if (data.mCfg.cID == 100)
            mOpPrij.VIdBr = dbuf[globalIndex++]

        mOp50Prij.RTCSinh = dbuf[globalIndex++]

        if (inCik) mOp50Prij.WDaySinh = dbuf[globalIndex++]
        else mOp50Prij.WDaySinh = 0

        if (data.mCfg.cID == 100) {
            globalIndex++
            globalIndex++
            globalIndex++
            globalIndex++
        } else if (data.mCfg.cID == 130 || data.mCfg.cID == 0x8C) {
            mOpPrij.VOpRe.StaPrij = dbuf[globalIndex++]
            mOpPrij.PromjZLjU = dbuf[globalIndex++]
        }

        for (i in 0..12)
            mOp50Prij.SinhTime[i] = setOprel4I(dbuf)


        if (!inCik)
            return

        mOpPrij.CRelXSw[0] = dbuf[globalIndex++]
        mOpPrij.VCRel1Tu = dbuf[globalIndex++]
        mOpPrij.VC1R1 = setOprelI(dbuf)

        mOpPrij.CRelXSw[1] = dbuf[globalIndex++]
        mOpPrij.VCRel2Tu = dbuf[globalIndex++]
        mOpPrij.VC1R2 = setOprelI(dbuf)

        mOpPrij.CRelXSw[2] = dbuf[globalIndex++]
        mOpPrij.VCRel3Tu = dbuf[globalIndex++]
        mOpPrij.VC1R3 = setOprelI(dbuf)

        mOpPrij.CRelXSw[3] = dbuf[globalIndex++]
        mOpPrij.VCRel4Tu = dbuf[globalIndex++]
        mOpPrij.VC1R4 = setOprelI(dbuf)


        if (data.mCfg.cID == 120 || m_HWVerPri == Const.Data.TIP_PS || m_HWVerPri == Const.Data.TIP_PSB)
            return

        mOpPrij.VAdrR1 = setVerAdrVer9(dbuf)
        mOpPrij.VAdrR2 = setVerAdrVer9(dbuf)
        mOpPrij.VAdrR3 = setVerAdrVer9(dbuf)
        mOpPrij.VAdrR4 = setVerAdrVer9(dbuf)

    }

    private fun setVerAdrVer9(dbuf: ByteArray)
            : Vadrr {
        val adrxx = dbuf[globalIndex++]
        return Vadrr().apply {
            VAdrRA = if (adrxx == 0.toByte()) 0 else getAdrNr(adrxx)
            VAdrRB = dbuf[globalIndex++]
            VAdrRC = dbuf[globalIndex++]
            VAdrRD = dbuf[globalIndex++]
        }
    }

    private fun getAdrNr(xxadr: Byte)
            : Byte {
        var i: Byte = 0
        while (i < 8) {
            if (xxadr == Const.Data.bVtmask[i.toInt()]) {
                return ++i
            }
            i++
        }
        return 0.toByte()
    }

    private fun getKlDatVer96(
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mOpPrij: Oprij,
        mReallocs: MutableList<Rreallc>
    ) {
        getKlDatVer9(dbuf, mOpPrij, mReallocs)
        mOp50Prij.apply {
            CPWBRTIME = setOprelI(dbuf)
            CLOGENFLGS[0] = setOprelI(dbuf).toShort()
            CLOGENFLGS[1] = setOprelI(dbuf).toShort()
            CLOGENFLGS[2] = setOprelI(dbuf).toShort()
        }
    }

    private fun getKlDatVer9(
        dbuf: ByteArray,
        mOpPrij: Oprij,
        mReallocs: MutableList<Rreallc>
    ) {
        mOpPrij.apply {
            KlOpR1 = setDlyRelDv9(dbuf)
            KlOpR2 = setDlyRelDv9(dbuf)
            KlOpR3 = setDlyRelDv9(dbuf)
            KlOpR4 = setDlyRelDv9(dbuf)
        }

        for (i in 0..3) {
            val reallc = Rreallc()
            reallc.rel_on = dbuf[globalIndex++]
            reallc.rel_off = dbuf[globalIndex++]
            mReallocs.add(reallc)
        }

    }

    private fun setDlyRelDv9(dbuf: ByteArray)
            : Klopr {
        return Klopr().apply {
            KRelDela = setOprel4I(dbuf)
            KRelDelb = setOprel4I(dbuf)
        }
    }


    private fun getLearningDat(dbuf: ByteArray)
            : List<StrLoadMng> {
        val strLoadMngs = mutableListOf<StrLoadMng>()
        for (i in 0..3) {
            val strLoadMng = getStrLoadMng(dbuf)
            strLoadMngs.add(strLoadMng)
        }
        return strLoadMngs
    }

    private fun getStrLoadMng(dbuf: ByteArray)
            : StrLoadMng {
        return StrLoadMng().apply {
            Status = dbuf[globalIndex++]
            relPos = dbuf[globalIndex++]
            TPosMin = setOprel3I(dbuf)
            TPosMax = setOprel3I(dbuf)
        }
    }


    private fun getTlgAbsenceDat(dbuf: ByteArray)
            : List<TlgAbstr> {
        val tlgAbstrs = mutableListOf<TlgAbstr>()
        for (i in 0..3) {
            val tlgAbstr = getTlgAbstr(dbuf)
            tlgAbstrs.add(tlgAbstr)
        }
        return tlgAbstrs
    }

    private fun getTlgAbstr(dbuf: ByteArray)
            : TlgAbstr {
        return TlgAbstr().apply {
            OnRes = dbuf[globalIndex++]
            TDetect = setOprel3I(dbuf)
            RestOn = dbuf[globalIndex++]
            OnTaExe = dbuf[globalIndex++]
        }
    }


    private fun getPonPoffRDat(dbuf: ByteArray)
            : List<PonPoffStr> {
        val ponPoffStrs = mutableListOf<PonPoffStr>()
        for (i in 0..3) {
            val ponPoffStr = setPonPoffReData(dbuf)
            ponPoffStrs.add(ponPoffStr)
        }
        return ponPoffStrs
    }

    private fun setPonPoffReData(dbuf: ByteArray)
            : PonPoffStr {
        return PonPoffStr().apply {
            OnPonExe = dbuf[globalIndex++]
            lperIgno = dbuf[globalIndex++]
            TminSwdly = setOprel3I(dbuf)
            TrndSwdly = setOprel3I(dbuf)
            Tlng = setOprel3I(dbuf)

            if ((Tlng and 0x800000) != 0)
                lperIgno = 0x80.toByte()
            else
                lperIgno = 0x00.toByte()

            Tlng = Tlng and 0x7fffff
            lOnPonExe = dbuf[globalIndex++]
            OnPoffExe = dbuf[globalIndex++]
            TBlockPrePro = setOprel3I(dbuf)
        }

    }

    private fun getWipers(dbuf: ByteArray)
            : List<Wiper> {
        val wipers = mutableListOf<Wiper>()
        for (i in 0..3) {
            val wiper = Wiper()
            wiper.status = (0x80 + 0x20).toByte() //TODO  unsigned char vs signed char    PAZZZIITT
            setWiperRelData(wiper, dbuf)
            wipers.add(wiper)
        }
        return wipers
    }

    private fun setWiperRelData(wiper: Wiper, dbuf: ByteArray) {
        wiper.status = dbuf[globalIndex++]
        wiper.Tswdly = setOprel3I(dbuf)
        wiper.TWiper = setOprel3I(dbuf)
        wiper.TBlockPrePro = setOprel3I(dbuf)
    }

    private fun getTparPar(dbuf: ByteArray, oPProg: Opprog): Opprog {
        val x = Unitimbyt()
        var nrTpar = 8

        val TIP_SPA = 2
        val NR_TPAR_SPA = 5
        val NR_TPAR_MAX = 14


        nrTpar = if (m_SWVerPri >= 90 && m_HWVerPri == TIP_SPA)
            NR_TPAR_SPA
        else if (m_SWVerPri in 40..95)
            NR_TPAR_MAX
        else
            data.mCfg.cNpar.toInt()


        x.b[1] = dbuf[globalIndex++]
        if (m_SWVerPri >= 40) x.b[0] = dbuf[globalIndex++]


        x.updateI()

        oPProg.AkTim = x.i
        oPProg.DanPr = dbuf[globalIndex++]

        for (i in 0..nrTpar) {
            x.i = setOprel3I(dbuf)
            x.updateTB()
            oPProg.TPro[i] = x.t
        }
        return oPProg
    }


    private fun getRelInterLock(dbuf: ByteArray)
            : List<IntrlockStr> {
        val intrLockStrList = mutableListOf<IntrlockStr>()
        for (rel in 0..3) {
            val intrLockStr =
                IntrlockStr(
                    setOprelI(dbuf),
                    setOprelI(dbuf),
                    intArrayOf(setOprelI(dbuf), setOprelI(dbuf))
                )
            intrLockStrList.add(intrLockStr)
        }
        for (rel in 0..3) {
            val intrLockStr = IntrlockStr()
            intrLockStrList.add(intrLockStr)
        }
        return intrLockStrList

    }

    private fun setOprelI(dbuf: ByteArray)
            : Int {
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, 0, 0))
        return tempi.i
    }

    private fun setOprel3I(dbuf: ByteArray)
            : Int {
        val b2 = dbuf[globalIndex++]
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, 0))
        return tempi.i
    }

    private fun setOprel4I(dbuf: ByteArray)
            : Int {
        val b3 = dbuf[globalIndex++]
        val b2 = dbuf[globalIndex++]
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, b3))
        return tempi.i
    }


    private fun getVersions(header: ByteArray) {
        val headString = header.toString(Charsets.UTF_8)

        var char: Char

        for ((cnt, version) in Const.Data.CTipPrij.withIndex()) {
            if (headString.contains(version, true)) {
                m_HWVerPri = cnt
                val indexOfVersionStart = headString.indexOf("V")
                char = headString.get(indexOfVersionStart + 1)
                if (char.isDigit())
                    m_SWVerPri = (char - '0') * 10
                char = headString.get(indexOfVersionStart + 3)
                if (char.isDigit())
                    m_SWVerPri += (char - '0')
                break
            }
            data.mHardwareVersion = m_HWVerPri
            data.mSoftwareVersion = m_SWVerPri
        }

        val startIndexOfParams = headString.indexOf(";")

        val buff = strCopyHexToBuf(headString, startIndexOfParams + 1)
        data.mCfg.cBrparam = buff[0]
        val a: Int = buff[1].toInt() and 0xFF
        val b: Int = buff[2].toInt() and 0xFF
        data.mCfg.cID = 256 * a + b

        data.mCfg.cPcbRev = buff[3]
        data.mCfg.cNrel = buff[4]
        data.mCfg.cRtc = buff[5]
        data.mCfg.cNprog = buff[6]
        data.mCfg.cNpar = buff[7]
    }


}


