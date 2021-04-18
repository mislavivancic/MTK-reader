package com.mtkreader.services

import android.content.Context
import android.util.Log
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.TIP_PA
import com.mtkreader.commons.Const.Data.TIP_PASN
import com.mtkreader.commons.Const.Data.TIP_PS
import com.mtkreader.commons.Const.Data.TIP_PSB
import com.mtkreader.commons.Const.Data.TIP_S
import com.mtkreader.commons.Const.Data.TIP_SN
import com.mtkreader.commons.Const.Data.TIP_SPA
import com.mtkreader.commons.Const.Data.TIP_SPN
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.data.reading.*
import com.mtkreader.utils.Css
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.HtmlTags.b
import com.mtkreader.utils.HtmlTags.bC
import com.mtkreader.utils.HtmlTags.body
import com.mtkreader.utils.HtmlTags.bodyC
import com.mtkreader.utils.HtmlTags.h2
import com.mtkreader.utils.HtmlTags.h2C
import com.mtkreader.utils.HtmlTags.htmlC
import com.mtkreader.utils.HtmlTags.table
import com.mtkreader.utils.HtmlTags.tableC
import com.mtkreader.utils.HtmlTags.td
import com.mtkreader.utils.HtmlTags.tdC
import com.mtkreader.utils.HtmlTags.tdImpAkt
import com.mtkreader.utils.HtmlTags.tdImpNeAkt
import com.mtkreader.utils.HtmlTags.tdImpNeutr
import com.mtkreader.utils.HtmlTags.th
import com.mtkreader.utils.HtmlTags.thC
import com.mtkreader.utils.HtmlTags.thcol2
import com.mtkreader.utils.HtmlTags.thcol2bgth
import com.mtkreader.utils.HtmlTags.thcol6
import com.mtkreader.utils.HtmlTags.thcol4
import com.mtkreader.utils.HtmlTags.thcol8
import com.mtkreader.utils.HtmlTags.tr
import com.mtkreader.utils.HtmlTags.trC
import com.mtkreader.utils.HtmlTags.thrw2
import com.mtkreader.utils.HtmlTags.thrw3
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.experimental.or

class ProcessDataServiceImpl : DisplayDataContract.ProcessService, KoinComponent {

    val context: Context by inject()

    // move this elsewhere

    var fVis_VersacomPS = false
    var fVis_Versacom = false
    var fVis_Uklsat = false
    var fVis_Prazdani = false
    var fVis_Sezone = false
    var fVis_Asat = false
    var fVis_RefPrij = false
    var fVis_TBAS = false
    var fVis_DUZADR = false
    var fVis_Realoc = false
    var fVis_Cz95P = false
    var fVis_Cz96P = false
    var fVis_Cz96HDOBAT = false

    var IsCZRaster = false
    var IsCZ44raster = false


    //private lateinit var REC_PAR_STR: REC_PAR_STRv
    // data to be filled
    private lateinit var wipers: List<Wiper>
    private lateinit var pOnPOffRDat: List<PonPoffStr>
    private lateinit var tlgAbsenceDat: List<TlgAbstr>
    private lateinit var learningData: List<StrLoadMng>
    private lateinit var mRelInterLock: List<IntrlockStr>
    private val mPProgR1 = Array(16) { Opprog() } //TODO array 16
    private val mPProgR2 = Array(16) { Opprog() }
    private val mPProgR3 = Array(16) { Opprog() }
    private val mPProgR4 = Array(16) { Opprog() }
    private val mOpPrij = Oprij()
    private val mOp50Prij = Oprij50()
    private val mReallocs = mutableListOf<Rreallc>()
    private val mTelegSync = mutableListOf<Telegram>()
    private val mTlgFnD = mutableListOf<Telegram>()
    private val mParFilteraCF = StrParFilVer9()
    private val mParFiltera = StrParFil()

    private val m_LoopParL = List(4) { m_LoopPar() }
    private val pLastPa = REC_PAR_STR()
    private var m_dwDeviceSerNr: Int = -1
    private var globalIndex = 0

    private val mline = ByteArray(256)
    private var m_Dateerr = 0
    private var m_cntxx = 1
    private var mBrojRast = 0
    private var mUtfPosto = 0.0
    private val UTFREFP = 0.9

    private var cntWork = 0
    private var isCheck = false

    private var mSoftwareVersionPri = 0
    private var m_HWVerPri = 0

    private var m_CFG: CfgParHwsw = CfgParHwsw()

    override fun processData(header: ByteArray, data: ByteArray): String {

        getVersions(header)

        initData()
        mline[0] = 0

        while (GetMsgLine(data)) {
            if (mline[0] != Const.Tokens.PARAM_READ_END_TOKEN.toByte()) {
                if (mline[0] == '!'.toByte()) break
                GetLineDat()
            }
            else
                break
        }

        setupFlags()
        return generateHtml(
            wipers,
            pOnPOffRDat,
            tlgAbsenceDat,
            learningData,
            mOpPrij
        )
    }

    private fun setupFlags() {
        fVis_VersacomPS = (m_HWVerPri != TIP_PS) && (m_HWVerPri != TIP_PSB)
        fVis_Versacom = m_HWVerPri != TIP_S && m_HWVerPri != TIP_SN && m_HWVerPri != TIP_SPN
        fVis_Uklsat =
            m_HWVerPri == TIP_SPA || m_HWVerPri == TIP_S || m_HWVerPri == TIP_SN || m_HWVerPri == TIP_SPN
        fVis_Prazdani =
            mSoftwareVersionPri >= 94 && (m_HWVerPri == TIP_S || m_HWVerPri == TIP_SN || m_HWVerPri == TIP_SPN)
        fVis_Sezone =
            (fVis_Versacom && m_HWVerPri != TIP_PA) || (fVis_Uklsat && mSoftwareVersionPri >= 95)
        fVis_Sezone =
            fVis_Sezone && mSoftwareVersionPri >= 80 && m_HWVerPri != TIP_PS && (m_HWVerPri != TIP_PSB)
        fVis_Asat = m_HWVerPri == TIP_PASN || m_HWVerPri == TIP_SN || m_HWVerPri == TIP_SPN
        fVis_RefPrij = m_HWVerPri != TIP_S && m_HWVerPri != TIP_SN
        fVis_TBAS = m_HWVerPri != TIP_PA
        fVis_DUZADR = m_HWVerPri != TIP_SPN
        fVis_Realoc = mSoftwareVersionPri >= 82

        fVis_Cz95P = mSoftwareVersionPri >= 95
        fVis_Cz96P = mSoftwareVersionPri >= 96
        fVis_Cz96HDOBAT = m_HWVerPri == TIP_PSB

    }
    private fun GetMsgLine(data: ByteArray)
            : Boolean {
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
        if(mline[0]==63.toByte())i++ //HACK TODO
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
                    3 -> mRelInterLock = getRelInterLock(dbuf) //TODO chekirat
                    4 -> getDeviceSerNr(dbuf)
                }
            }
            1 -> getTparPar(dbuf,mPProgR1[mgaddr.objectt])
            2 -> getTparPar(dbuf,mPProgR2[mgaddr.objectt])
            3 -> getTparPar(dbuf,mPProgR3[mgaddr.objectt])
            4 -> getTparPar(dbuf,mPProgR4[mgaddr.objectt])

            0xC -> getFriRPar(dbuf, mParFilteraCF, mParFiltera)

            7 -> {
                // PRAZNO ZA PS i PSB
            }
            5 -> {
                when (mgaddr.objectt) {
                    0 -> wipers = getWipers(dbuf)
                    1 -> pOnPOffRDat = getPonPoffRDat(dbuf)
                    2 -> tlgAbsenceDat = getTlgAbsenceDat(dbuf)
                    3 -> learningData = getLearningDat(dbuf)
                }
            }
            6 -> GetLoopTim(dbuf) //TODO implementirati

            8 -> getOprijParV9(mgaddr, dbuf, mOp50Prij, mOpPrij, mReallocs)
            9 -> getTlg50Par(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnD)
        }

    }

    private fun getDeviceSerNr(dbuf: ByteArray) {

        m_dwDeviceSerNr = setOprel4I(dbuf)

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
            m_LoopParL[uRel].State = dbuf[globalIndex++]
            m_LoopParL[uRel].LastCmd = dbuf[globalIndex++]
            m_LoopParL[uRel].Tpar.ton = setOprelI(dbuf).toShort()
            m_LoopParL[uRel].Tpar.toff = setOprelI(dbuf).toShort()
        }
        m_LoopParL
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


        for (i in 0..REC_PAR_STR.DataTime_SIZE - 1) pLastPa.DataTime[i] = dbuf[globalIndex++]
        for (i in 0..REC_PAR_STR.PARID_SIZE - 1) pLastPa.CreateSite[i] = dbuf[globalIndex++]
        for (i in 0..REC_PAR_STR.PARID_SIZE - 1) pLastPa.IDCreate[i] = dbuf[globalIndex++]
        for (i in 0..REC_PAR_STR.PARID_SIZE - 1) pLastPa.ReParaSite[i] = dbuf[globalIndex++]
        for (i in 0..REC_PAR_STR.PARID_SIZE - 1) pLastPa.IDRePara[i] = dbuf[globalIndex++]
        for (i in 0..REC_PAR_STR.PARIDFILE_SIZE - 1) pLastPa.IDFile[i] = dbuf[globalIndex++]

        var s = pLastPa.toString()


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

    private fun initData() {
        //for (i in 0..15) {
        //    mPProgR1.add(Opprog())
        //    mPProgR2.add(Opprog())
        //    mPProgR3.add(Opprog())
        //    mPProgR4.add(Opprog())
        //}
        for (i in 0..12)
            mTelegSync.add(Telegram())

        for (i in 0..7)
            mTlgFnD.add(Telegram())

    }

    private fun generateHtml(
        wipers: List<Wiper>,
        ponPoffstrs: List<PonPoffStr>,
        tlgAbstrs: List<TlgAbstr>,
        strLoadMngs: List<StrLoadMng>,
        oprij: Oprij
    ): String {
        val htmlBuilder = StringBuilder()
        htmlBuilder.append(Css.htmlstart)
        var title="Naslov"
        htmlBuilder.append("<title>"+ title+"</title>")
        htmlBuilder.append("<style>"+ getString(R.string.ccs6min)+"</style>")
        //htmlBuilder.append("<style>"+ Css.cssx+"</style>")
        htmlBuilder.append("</head>")
        htmlBuilder.append(body)
        htmlBuilder.append("<div class=\"container\">")
        generateContent(
            htmlBuilder,
            wipers,
            ponPoffstrs,
            tlgAbstrs,
            strLoadMngs,
            oprij
        )
        htmlBuilder.append("</div>$bodyC$htmlC")

        return htmlBuilder.toString()
    }

    private fun generateContent(
        builder: java.lang.StringBuilder,
        wipers: List<Wiper>,
        ponPoffstrs: List<PonPoffStr>,
        tlgAbstrs: List<TlgAbstr>,
        strLoadMngs: List<StrLoadMng>,
        oprij: Oprij
    ) {
        generateGeneral(builder)


        generateRelaySettings(builder, oprij)

        if (fVis_Realoc)
            generateRelaySwitchAssignement(builder)

        if (fVis_RefPrij) {
            generateSwitchingDelay(builder, oprij)
            generateClassicTelegram(builder)
        }

        if (fVis_Cz96P) {
            generateAdditionalTelegram(builder)
        }

        if (fVis_RefPrij && fVis_Cz95P) {
            generateTelegramSync(builder)
            //generateSyncTelegramDoW(builder)
        }

        generateWorkSchedules(builder,  oprij)
        generateWiperAndClosedLoop(builder, wipers)
        generateLearnFunctions(builder, strLoadMngs)
        generateTalegramAbsence(builder, tlgAbstrs)
        generateArrivalAndLossOfSupply(builder, ponPoffstrs)
        generateEventLog(builder)

        generateInterlock(builder)

    }

    private fun unPackLadderString(rel: Int, pccNfg: Int): String {
        val state = mutableListOf(0.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
        val varRel = mutableListOf(0.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
        val rr = mutableListOf("", "", "", "")

        val R_NC=2.toByte()
        val R_NO=1.toByte()


        var tmp = (pccNfg shr 13) and 0x07
        if (tmp != 0) {
            varRel[0] = (tmp and 0x3).toByte()
            state[0] = if ((tmp and 0x04) != 0) R_NC else R_NO
            val str = if ((tmp and 0x04) != 0) getString(R.string.not) else ""
            val num = tmp and 0x3
            rr[0]=String.format("%s R%d", str, num)
        }

        tmp = (pccNfg shr 10) and 0x07
        if (tmp != 0) {
            varRel[2] = (tmp and 0x3).toByte()
            state[2] = if ((tmp and 0x04) != 0) R_NC else R_NO
            val str = if ((tmp and 0x04) != 0) getString(R.string.not) else ""
            val num = tmp and 0x3
            rr[2]=String.format("%s R%d", str, num)
        }
        tmp = (pccNfg shr 7) and 0x07
        if (tmp != 0) {
            varRel[1] = (tmp and 0x3).toByte()
            state[1] = if ((tmp and 0x04) != 0) R_NC else R_NO
            val str = if ((tmp and 0x04) != 0) getString(R.string.not) else ""
            val num = tmp and 0x3
            rr[1]=String.format("%s R%d", str, num)
        }
        tmp = (pccNfg shr 4) and 0x07
        if (tmp != 0) {
            varRel[3] = (tmp and 0x3).toByte()
            state[3] = if ((tmp and 0x04) != 0) R_NC else R_NO
            val str = if ((tmp and 0x04) != 0) getString(R.string.not) else ""
            val num = tmp and 0x3
            rr[3]=String.format("%s R%d", str, num)
        }

        val idx = pccNfg and 0x0F
        var isFound = false
        var indexFound = 0
        for (i in 0..11)
            if (DataUtils.getPLCfg(i).Idx == idx) {
                isFound = true
                indexFound = i
                break
            }

        val ladderStates = mutableListOf<LadderState>()
        for (i in 0..7)
            ladderStates.add(LadderState())

        if (isFound) {
            for (i in 0..3) {
                ladderStates[0].m_RelState[i] = (DataUtils.getPLCfg(indexFound).RelState[i].toInt() and state[i].toInt()).toByte()
                ladderStates[0].m_RelNr[i] = (DataUtils.getPLCfg(indexFound).RelNr[i].toInt() and varRel[i].toInt()).toByte()
                ladderStates[0].m_isSeries[i] = DataUtils.getPLCfg(indexFound).isSeries[i]
                ladderStates[0].m_notsernotpar[i] = DataUtils.getPLCfg(indexFound).notsernotpar[i]
                ladderStates[0].m_conectshort[i] = DataUtils.getPLCfg(indexFound).conectshort[i]
            }
        }

        var res = ""
        var relst = 0
        var relser = 0
        for (i in 0..3) {
            if (ladderStates[0].m_RelNr[i].toInt() != 0) relst = relst or (1 shl i)
            if (ladderStates[0].m_isSeries[i]) relser = relser or (1 shl i)
        }

        if (relst == 0b0000)
            res = getString(R.string.none).toUpperCase() + rr[0] + rr[1] + rr[2] + rr[3]
        else if (relst == 0b0111) {
            if (ladderStates[0].m_conectshort[3])
                res = String.format("(%s ${getString(R.string.and)} %s) ${getString(R.string.or)} %s ", rr[0], rr[2], rr[1])
            else
                res = String.format("(%s ${getString(R.string.or)} %s) ${getString(R.string.and)} %s ", rr[0], rr[1], rr[2])
        } else if (relst == 0b1101) {
            if (ladderStates[0].m_conectshort[1])
                res = String.format("(%s ${getString(R.string.and)} %s) ${getString(R.string.or)} %s ", rr[0], rr[2], rr[3])
            else
                res = String.format("%s ${getString(R.string.and)} (%s ${getString(R.string.or)} %s) ", rr[0], rr[2], rr[3])
        }
        else if (relst == 0b0011) res = String.format("%s ${getString(R.string.or)} %s", rr[0], rr[1])
        else if (relst == 0b1100) res = String.format("%s ${getString(R.string.or)} %s", rr[2], rr[3])
        else if (relst == 0b0001) res = String.format("%s", rr[0])
        else if (relst == 0b0100) res = String.format("%s", rr[2])
        else if (relst == 0b0101) res = String.format("%s ${getString(R.string.and)} %s", rr[0], rr[2])
        else if (relst == 0b1111)
            if (ladderStates[0].m_isSeries[0])
                res = String.format("(%s ${getString(R.string.and)} %s) ${getString(R.string.or)} (%s ${getString(R.string.and)}  %s)", rr[0], rr[1], rr[2], rr[3])
            else
                res = String.format("(%s ${getString(R.string.or)} %s) ${getString(R.string.and)} (%s ${getString(R.string.or)}  %s)", rr[0], rr[1], rr[2], rr[3])

        return res
    }

    private fun generateInterlock(builder: java.lang.StringBuilder) {

        builder.append(h2 + getString(R.string.logic_function) + h2C)
        builder.append(table)
        for (rel in 0..5) {
            val cfg = mRelInterLock[rel].PcCnfg[0]
            val res = unPackLadderString(rel, cfg)
            if (rel < 3) {
                builder.append(tr + th + String.format("R%d %s", rel + 1, getString(R.string.a)) + thC + td + res + tdC + trC)
            } else {
                builder.append(tr + th + String.format("R%d %s", rel % 3 + 1, getString(R.string.b)) + thC + td + res + tdC + trC)
            }
        }
        builder.append(tableC)
    }

    private fun generateEventLog(builder: java.lang.StringBuilder) {
        val mLogEnFlags = arrayOf(mOp50Prij.CLOGENFLGS[0].toInt(), mOp50Prij.CLOGENFLGS[1].toInt())
        val strYes=getString(R.string.yes)
        val strNo=getString(R.string.no)

        builder.append(h2 + getString(R.string.event_log) + h2C)
        builder.append(table)

        ///---------------------------
        builder.append(tr + thcol2bgth + getString(R.string.common_log) + thC + trC)

        var yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_POFF) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.power_on_off_time) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_SHT) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.synchronization_telegram_time) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_SHD) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.synchronization_telegram_day) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_LSINH) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.local_change_of_time) + tdC + td + yesNo + tdC + trC)

        ///---------------------------
        builder.append(tr + thcol2bgth + getString(R.string.rtc_log) + thC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_RTC_OF) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.oscillator_fail) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_RTC_ST) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.rtc_stop) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.SNE_RTC_BL) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.battery_low) + tdC + td + yesNo + tdC + trC)

        ///---------------------------
        builder.append(tr + thcol2bgth + getString(R.string.relay_log) + thC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.REL_ON) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.relay_switched_by_telegram) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.PRO_REL_X) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.relay_switched_by_program) + tdC + td + yesNo + tdC + trC)

       // yesNo = if ((mLogEnFlags[0] and Const.Data.REL_WIP_S) != 0) strYes else strNo
       // builder.append(tr + td + getString(R.string.start_wiper) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[1] and Const.Data.REL_WIP_R) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.end_wiper) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[1] and Const.Data.REL_TA_S) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.telegram_absence_start) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[1] and Const.Data.REL_TA_R) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.telegram_absence_restart) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[1] and Const.Data.REL_PROBLOCK) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.work_schedule_disabled) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[1] and Const.Data.REL_PROUNBLOCK) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.work_schedule_enabled) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[1] and Const.Data.PON_REL_X) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.relay_switched_by_pwron) + tdC + td + yesNo + tdC + trC)

        ///---------------------------
        builder.append(tr + thcol2bgth + getString(R.string.telegram_log) + thC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.OPT_LOG_TLG) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.log_all_telegrams) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.OPT_LOG_MYTLG) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.log_all_telegrams_for_this_rec) + tdC + td + yesNo + tdC + trC)

        yesNo = if ((mLogEnFlags[0] and Const.Data.OPT_LOG_REPTLG) != 0) strYes else strNo
        builder.append(tr + td + getString(R.string.log_only_telegrams) + tdC + td + yesNo + tdC + trC)

        builder.append(tableC)
    }

    private fun generateArrivalAndLossOfSupply(
        builder: java.lang.StringBuilder,
        ponPoffstrs: List<PonPoffStr>
    ) {
        builder.append(h2 + getString(R.string.arrival_and_loss_of_supply) + h2C)
        builder.append(table)
        builder.append(tr)
        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.stop_learn_and_wiper_functions) + thC)
        for (i in 0..3)
            if ((ponPoffstrs[i].OnPonExe.toInt() and Const.Data.PON_DISLRN_I_W_MASK) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.power_supply_loss_time) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(ponPoffstrs[i].Tlng) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.loss_of_supply_short) + thC)
        for (i in 0..3) {
            val ss = ponPoffstrs[i].OnPonExe.toInt() and 0x7F
            if (ss < 10)
                builder.append(td + getStringArray(R.array.poffS, ss) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        }
        builder.append(trC)

        val ign = mutableListOf(0, 0, 0, 0)
        builder.append(tr)
        builder.append(th + getString(R.string.loss_of_supply_long_ignore) + thC)
        for (i in 0..3)
            if ((ponPoffstrs[i].lperIgno.toInt() and Const.Data.PON_LPERIOD_DIS_MASK) != 0) {
                builder.append(td + getString(R.string.yes) + tdC)
                ign[i] = 1
            } else
                builder.append(td + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.loss_of_supply_long_action) + thC)
        for (i in 0..3) {
            val ss = ponPoffstrs[i].lOnPonExe.toInt()
            if (ign[i] == 1) {
                builder.append("$td/$tdC")
                continue
            }
            if (ss < 6)
                builder.append(td + getStringArray(R.array.poffL, ss) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        }
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.switch_delay_min) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(ponPoffstrs[i].TminSwdly) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.switch_delay_max) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(ponPoffstrs[i].TrndSwdly) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.scheduled_switching_activation_delay) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(ponPoffstrs[i].TBlockPrePro) + tdC)
        builder.append(trC)


        builder.append(tr)
        builder.append(th + getString(R.string.action) + thC)
        for (i in 0..3) {
            val ss = ponPoffstrs[i].OnPoffExe.toInt()
            if (ss < 3)
                builder.append(td + getStringArray(R.array.poffAct, ss) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        }
        builder.append(trC)

        builder.append(tableC)
    }

    private fun generateTalegramAbsence(
        builder: java.lang.StringBuilder,
        tlgAbstrs: List<TlgAbstr>
    ) {
        builder.append(h2 + getString(R.string.telegram_absence) + h2C)
        builder.append(table)
        builder.append(tr)
        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.absence_time) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(tlgAbstrs[i].TDetect) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.rst_timer_on) + thC)
        for (i in 0..3)
            if (tlgAbstrs[i].RestOn < 0x0F)
                builder.append(td + getStringArray(R.array.rst, tlgAbstrs[i].RestOn.toInt()) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.on_timer_restart) + thC)
        for (i in 0..3)
            if ((tlgAbstrs[i].OnRes.toInt() and 0x01) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.action) + thC)
        for (i in 0..3) {
            val ss = (tlgAbstrs[i].OnTaExe.toInt() and 0x0F)
            if (ss < 0x0F)
                builder.append(td + getStringArray(R.array.act, ss) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        }
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.learn_function_disabled) + thC)
        for (i in 0..3)
            if ((tlgAbstrs[i].OnTaExe.toInt() and Const.Data.TLGA_ON_DISLRN) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tableC)
    }

    private fun generateLearnFunctions(
        builder: java.lang.StringBuilder,
        strLoadMngs: List<StrLoadMng>
    ) {
        builder.append(h2 + getString(R.string.learn_functions) + h2C)
        builder.append(table)
        builder.append(tr)
        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.learn_period) + thC)
        for (i in 0..3)
            if ((strLoadMngs[i].Status.toInt() and Const.Data.LEARN_7DAYS_MASK) == 0)
                builder.append(td + getString(R.string.day_h) + tdC)
            else
                builder.append(td + getString(R.string.seven_days) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.position) + thC)

        for (i in 0..3)
            if ((strLoadMngs[i].relPos.toInt() and Const.Data.LEARN_R_ON_MASK) != 0)
                builder.append(td + getString(R.string.a) + tdC)
            else if ((strLoadMngs[i].relPos.toInt() and Const.Data.LEARN_R_OFF_MASK) != 0)
                builder.append(td + getString(R.string.b) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.min) + thC)
        for (i in 0..3)
            builder.append(td + getHMfromInt(strLoadMngs[i].TPosMin) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.max) + thC)
        for (i in 0..3)
            builder.append(td + getHMfromInt(strLoadMngs[i].TPosMax) + tdC)
        builder.append(trC)

        builder.append(tableC)
    }

    private fun generateWiperAndClosedLoop(builder: StringBuilder, wipers: List<Wiper>) {

        builder.append(h2 + getString(R.string.wiper_and_closed_loop) + h2C)
        builder.append(table)
        builder.append(tr)
        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.wiper_enable) + thC)
        for (i in 0..3)
            if ((wipers[i].status.toInt() and Const.Data.WIPER_DISEB_MASK) == 0)
                builder.append(tdImpAkt + getString(R.string.yes) + tdC)
            else
                builder.append(tdImpNeAkt + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.retrigerable) + thC)
        for (i in 0..3)
            if ((wipers[i].status.toInt() and Const.Data.WIPPER_RETRIG_MASK) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.activation_command) + thC)
        for (i in 0..3)
            if ((wipers[i].status.toInt() and Const.Data.WIPER_ON_MASK) != 0)
                builder.append(td + getString(R.string.a) + tdC)
            else if ((wipers[i].status.toInt() and Const.Data.WIPER_OFF_MASK) != 0)
                builder.append(td + getString(R.string.b) + tdC)
            else
                builder.append(td + getString(R.string.xx) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.switching_delay) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(wipers[i].Tswdly) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.wiper_time) + thC)
        for (i in 0..3)
            builder.append(td + getDHMSfromInt(wipers[i].TWiper) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.scheduled_switching_activation_delay) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(wipers[i].TBlockPrePro) + tdC)
        builder.append(trC)

        //TODO LOOP--------------------
        builder.append(tr)
        builder.append(th + getString(R.string.loop_enable) + thC)
        for (i in 0..3)
            if ((wipers[i].status.toInt() and Const.Data.LOOP_DISEB_MASK) == 0)
                builder.append(tdImpAkt + getString(R.string.yes) + tdC)
            else
                builder.append(tdImpNeAkt + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.duration_in_position) + thC)
        for (i in 0..3)
            builder.append(td + getDHMSfromInt(wipers[i].TWiper) + tdC)
        builder.append(trC)

        builder.append(tableC)
    }

    private fun getDHMSfromInt(time: Int): String { //TODO check signed
        var time = time
        val hh: Int
        val mm: Int
        val ss: Int
        val dd:Int =time/(24*3600)
        time=time%(24*3600)
        ss = time % 60
        time /= 60
        mm = time % 60
        hh = time / 60
        return String.format("%d d %02d:%02d:%02d", dd,hh, mm, ss)
    }

    private fun getHMSfromInt(time: Int): String {
        var time = time
        val hh: Int
        val mm: Int
        val ss: Int
        if (time >= 24 * 3600) {
            time = 0
        }
        ss = time % 60
        time /= 60
        mm = time % 60
        hh = time / 60
        return String.format("%02d:%02d:%02d", hh, mm, ss)
    }

    private fun getHMfromInt(time: Int): String {
        var time = time
        val hh: Int
        val mm: Int
        val ss: Int
        if (time >= 24 * 60) {
            time = 0
        }
        mm = time % 60
        hh = time / 60
        ss = 0
        return String.format("%02d:%02d:%02d", hh, mm, ss)
    }

    private fun generateWorkSchedules(
        builder: java.lang.StringBuilder,
        oprij: Oprij
    ) {

        if (fVis_Versacom) {
            val buildersWorkSchedTimePairs = mutableListOf<StringBuilder>()
            for (relay in 0..3) {
                if ((oprij.VOpRe.StaPrij.toInt() and (0x80 shr relay)) == 0)continue
                when (relay) {
                    0 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR1))
                    1 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR2))
                    2 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR3))
                    3 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR4))
                }
            }
            val buildersWorkSchedTimeDays = mutableListOf<StringBuilder>()

            for (relay in 0..3) {
                if ((oprij.VOpRe.StaPrij.toInt() and (0x80 shr relay)) == 0)continue
                when (relay) {
                    0 -> buildersWorkSchedTimeDays.add(getRelAkProg(mPProgR1))
                    1 -> buildersWorkSchedTimeDays.add(getRelAkProg(mPProgR2))
                    2 -> buildersWorkSchedTimeDays.add(getRelAkProg(mPProgR3))
                    3 -> buildersWorkSchedTimeDays.add(getRelAkProg(mPProgR4))
                }
            }

            for (i in 0..3) {
                builder.append(h2 + getString(R.string.work_schedules_time_pairs) + (i + 1) + h2C)
                if (buildersWorkSchedTimeDays.lastIndex >= i)
                    builder.append(buildersWorkSchedTimeDays[i].toString())
                if (buildersWorkSchedTimePairs.lastIndex >= i)
                    builder.append(buildersWorkSchedTimePairs[i].toString())
            }
        }
    }

    private fun getRelAkProg(mPProgR: Array<Opprog>): StringBuilder {
        val builder = StringBuilder()
        val temp = mutableListOf("", "", "", "", "", "", "", "")
        builder.append(table)
        builder.append(tr)
        builder.append(th + getString(R.string.work_schedules) + thC)
        builder.append(th + getString(R.string.active) + thC)

        for (i in 0..7)
            builder.append(th + getStringArray(R.array.a_days, i) + thC)
        builder.append(trC)

        for (pItem in 0..15) {
            if (mPProgR[pItem].AkTim != 0) {
                builder.append(tr)
                builder.append(td + (pItem + 1) + tdC)
                val yesNo = if ((DataUtils.getIVtmask(pItem) and mOpPrij.VOpRe.VakProR1) != 0)
                    getString(R.string.yes)
                else
                    getString(R.string.no)
                builder.append(td + yesNo + tdC)

                for (iItem in 7 downTo 0)
                    if ((DataUtils.getBVtmask(iItem) and mPProgR[pItem].DanPr.toInt()) != 0)
                        temp[iItem] = "+"
                    else
                        temp[iItem] = "-"

                for (i in 7 downTo 0)
                    builder.append(td + temp[i] + tdC)
                builder.append(trC)
            }
        }
        return builder
    }



    private fun showTimePairs(mPProgR: Array<Opprog>): StringBuilder {
        val timePairsTableBuilder = StringBuilder()
        timePairsTableBuilder.append(table)
        timePairsTableBuilder.append(tr)
        timePairsTableBuilder.append(th + getString(R.string.work_scheds) + thC)
        timePairsTableBuilder.append(th + getString(R.string.time_pair) + thC)
        timePairsTableBuilder.append(th + getString(R.string.t_a_par) + thC)
        timePairsTableBuilder.append(th + getString(R.string.t_b_par) + thC)
        timePairsTableBuilder.append(trC)
        cntWork = 0
        for (rp in 0..15)
            getRelVremPar(timePairsTableBuilder, rp, mPProgR)
        timePairsTableBuilder.append(tableC)
        return timePairsTableBuilder
    }

    private fun getRelVremPar(builder: StringBuilder, rp: Int, mPProgR: Array<Opprog>) {
        var count = 0
        for (itemIndex in 0..m_CFG.cNpar) {
            if ((mPProgR[rp].AkTim and DataUtils.getIVtmask(itemIndex)) != 0) {
                builder.append(tr)
                count++
                if (count == 1) {
                    builder.append(td + (cntWork + 1) + tdC)
                    cntWork++
                } else
                    builder.append(td + tdC)

                builder.append(td + String.format("%02d", itemIndex + 1) + tdC)
                val Ton= String.format("%02d:%02d", (mPProgR[rp].TPro[itemIndex].Ton) / 60, (mPProgR[rp].TPro[itemIndex].Ton) % 60)
                val Toff= String.format("%02d:%02d", (mPProgR[rp].TPro[itemIndex].Toff) / 60, (mPProgR[rp].TPro[itemIndex].Toff) % 60)
                val Tblank= "--:--"

                var Tm:String=""

                if(mPProgR[rp].TPro[itemIndex].Tonb!=1) Tm=Ton else Tm=Tblank
                builder.append(td + Tm+ tdC)

                if(mPProgR[rp].TPro[itemIndex].Toffb!=1) Tm=Toff else Tm=Tblank
                builder.append(td + Tm+ tdC)

                builder.append(trC)
            }
        }
    }


    private fun generateTelegramSync(
        builder: java.lang.StringBuilder
    ) {
        builder.append(h2 + getString(R.string.sync_telegrams) + h2C)
        builder.append(table)

        builder.append(tr)
        SetRasterHead(builder,getString(R.string.IDSI_COL_NAME), getString(R.string.telegram))
        builder.append(trC)

        for (i in 0..7) {
            builder.append(tr)
                builder.append(th+"DBQ:GetTlgNameByContent"+ thC)
                builder.append(th + getSyncTime(mOp50Prij.SinhTime[i], m_HWVerPri) + thC)
                getRasterString(builder, mTelegSync[i].Cmd)
        }
        builder.append(tableC)
    }

    private fun generateAdditionalTelegram(
        builder: java.lang.StringBuilder
    ) {
        builder.append(h2 + getString(R.string.additional_telegrams) + h2C)
        builder.append(table)

        builder.append(tr)
        SetRasterHead(builder,getString(R.string.IDSI_COL_NAME), getString(R.string.telegram))
        builder.append(trC)

        var TGFN= arrayOf( R.string.IDSCB_FN_TELEG1_0 ,R.string.IDSCB_FN_TELEG1_6 ,R.string.IDSCB_FN_TELEG1_7 ,R.string.IDSCB_FN_TELEG1_18 ,R.string.IDSCB_FN_TELEG1_8 ,R.string.IDSCB_FN_TELEG1_9 ,R.string.IDSCB_FN_TELEG1_10 ,R.string.IDSCB_FN_TELEG1_11 ,R.string.IDSCB_FN_TELEG1_12 ,R.string.IDSCB_FN_TELEG1_13 ,R.string.IDSCB_FN_TELEG1_14 ,R.string.IDSCB_FN_TELEG1_15 ,R.string.IDSCB_FN_TELEG1_16 ,R.string.IDSCB_FN_TELEG1_17)

        if(fVis_Cz96P) {
            for(i in 0..7)
            {
                builder.append(tr)
                builder.append(th+"DBQ:GetTlgNameByContent"+ thC)
                var f:Int=mOp50Prij.tlg[i].tel1.Cmd.Fn.toInt()

                if (i in 0..6)
                    builder.append(th+getString(TGFN[f])+thC)

                if(i==7){
                    if(f==0x40) builder.append(th +getString(R.string.IDSI_EMERGTLG)+ " '" +getString(R.string.IDSI_OFF)+"'"+thC) //AKT_SET_OFF
                    if(f==0x80) builder.append(th +getString(R.string.IDSI_EMERGTLG)+ " '" +getString(R.string.IDSI_ON)+"'"+thC) //AKT_SET_ON
                }

                getRasterString(builder, mOp50Prij.tlg[i].tel1.Cmd)
                builder.append(trC)
            }
        }
        builder.append(tableC)
    }

    private fun generateClassicTelegram(builder: StringBuilder) {
        builder.append(h2 + getString(R.string.classic_telegram) + h2C)
        builder.append(table)

        builder.append(tr)
        SetRasterHead(builder,getString(R.string.IDSI_COL_NAME), getString(R.string.telegram))
        builder.append(trC)

        GetRelTlgs(builder,mOp50Prij.TlgRel1,1)
        GetRelTlgs(builder,mOp50Prij.TlgRel2,2)
        GetRelTlgs(builder,mOp50Prij.TlgRel3,3)
        GetRelTlgs(builder,mOp50Prij.TlgRel4,4)

        builder.append(tableC)
    }


    private fun getRasterString(builder: StringBuilder, t: TelegCMD) {

        for (iBimp in 0..49) {
            val nBitNumber = iBimp % 8
            val nByteNumber = iBimp / 8

            val N = t.NeutImp[nByteNumber].toInt() and (0x80 shr nBitNumber)
            val A = t.AktiImp[nByteNumber].toInt() and (0x80 shr nBitNumber)

            if (IsCZ44raster && iBimp == 44) break

            if (A != 0 && N != 0) builder.append(tdImpNeAkt + b + getString(R.string.plus) + bC + tdC)
            else if (A == 0 && N != 0) builder.append(tdImpAkt + b + getString(R.string.minus) + bC + tdC)
            else builder.append(tdImpNeutr + tdC)
        }

    }


    private fun getSyncTime(t: Int, ver: Int): String? {
        var datstr: String
        var str: String=""
        var tstr: String
        var dstr: String
        var stime: Int
        var tmpi: Int
        if (ver == TIP_PA) {
            stime = t and 0x000FFFFF
            datstr = String.format(
                "%02d:%02d:%02d",
                stime / 60 / 60 % 24,
                stime / 60 % 60,
                stime % 60
            )
            tmpi = stime / 60 / 60 / 24
            if (t and 0x00800000 == 0) {
                tmpi = 7
            }
        } else {
            val rtctime = Uni4byt(t)
            tstr = String.format("%02d:%02d:%02d", rtctime.b?.get(2)!!.toInt() and 0x1F, rtctime.b?.get(1)!!.toInt(), rtctime.b?.get(0)!!.toInt())
            var b3:Int=rtctime.b!![3].toInt()

            var sday=b3 and 0x07
            if(sday==0) sday=7 else sday=sday-1
            dstr=getStringArray(R.array.dan_sync_tg, sday % 8)
            if( ( b3 and 0x40) >0 ) //samo dan
            {
                if(sday==7) str="---"
                else str = dstr
            }
            else if(b3==0) str=tstr                 //samo vrijeme
            else if( ( b3 and 0x80) >0 ) str="---"  //nijedno
            else str= tstr + " " + dstr             //oboje


        }
        return str
    }

    private fun GetRasterHeadStringABDPC(builder: StringBuilder) {

        builder.append(thcol4 + getString(R.string.a) + thC)
        builder.append(thcol8 + getString(R.string.b) + thC)
        for (i in 1..16) builder.append(thcol2 + String.format("DP%d", i) + thC)
        if (!IsCZ44raster) builder.append(thcol6 + getString(R.string.c) + thC)

    }

    private fun GetRasterHeadStringNUM(builder: StringBuilder, isCz:Boolean) {
        if(isCz){
            for (i in 1..50) {
                if(IsCZ44raster && i>44) break
                builder.append(th + i + thC)
            }
        }else {
            for (i in 1..4) builder.append(th + i + thC)
            for (i in 1..8) builder.append(th + i + thC)

            for (i in 14..45 step 2) {
                builder.append(th + getString(R.string.z) + thC)
                builder.append(th + getString(R.string.v) + thC)
            }
            if (!IsCZ44raster)
                for (i in 1..6) builder.append(th + i + thC)
        }

    }
    private fun SetRasterHead(builder: StringBuilder, s1:String, s2:String) {
        if (IsCZRaster) {
            builder.append(tr);
                builder.append(thrw3+ s1 +thC)
                builder.append(thrw3+ s2 +thC)
                GetRasterHeadStringABDPC(builder);
            builder.append(trC);
            builder.append(tr);GetRasterHeadStringNUM(builder,false);builder.append(trC);
            builder.append(tr);GetRasterHeadStringNUM(builder,true);builder.append(trC);
        }
        else{
            builder.append(tr);GetRasterHeadStringNUM(builder,false);builder.append(trC);
            builder.append(tr+trC);
            builder.append(tr+trC);
        }

    }

    private fun GetRelTlgs(builder: StringBuilder, T:Telegrel,index:Int) {
        builder.append(tr)
            builder.append(thrw2+"DBQ:GetTlgNameByContentK"+ thC)
            builder.append(th+"R"+index+getString(R.string.IDSI_ON)+ thC)
            getRasterString(builder,T.Uk)
        builder.append(trC)

        builder.append(tr)
            builder.append(th+"R"+index+getString(R.string.IDSI_OFF)+ thC)
            getRasterString(builder,T.Isk)
        builder.append(trC)
    }

    private fun getRasterHeadStringBottom(builder: StringBuilder) {
        builder.append(tr)
        builder.append(th + thC)
        builder.append(th + thC)

        for (i in 1..44) {
            builder.append(th + i + thC)
        }
        builder.append(trC)
    }

    private fun generateSwitchingDelay(builder: StringBuilder, oprij: Oprij) {
        builder.append(h2 + getString(R.string.switching_delay) + h2C)
        builder.append(table)
        builder.append(tr)

        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)

        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.delay_a) + thC)
        builder.append(td + getZatez(oprij.KlOpR1?.KRelDela, 't') + tdC)
        builder.append(td + getZatez(oprij.KlOpR2?.KRelDela, 't') + tdC)
        builder.append(td + getZatez(oprij.KlOpR3?.KRelDela, 't') + tdC)
        builder.append(td + getZatez(oprij.KlOpR4?.KRelDela, 't') + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.delay_a) + thC)
        builder.append(td + getZatez(oprij.KlOpR1?.KRelDela, 'm') + tdC)
        builder.append(td + getZatez(oprij.KlOpR2?.KRelDela, 'm') + tdC)
        builder.append(td + getZatez(oprij.KlOpR3?.KRelDela, 'm') + tdC)
        builder.append(td + getZatez(oprij.KlOpR4?.KRelDela, 'm') + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.delay_b) + thC)
        builder.append(td + getZatez(oprij.KlOpR1?.KRelDelb, 't') + tdC)
        builder.append(td + getZatez(oprij.KlOpR2?.KRelDelb, 't') + tdC)
        builder.append(td + getZatez(oprij.KlOpR3?.KRelDelb, 't') + tdC)
        builder.append(td + getZatez(oprij.KlOpR4?.KRelDelb, 't') + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.delay_b) + thC)
        builder.append(td + getZatez(oprij.KlOpR1?.KRelDelb, 'm') + tdC)
        builder.append(td + getZatez(oprij.KlOpR2?.KRelDelb, 'm') + tdC)
        builder.append(td + getZatez(oprij.KlOpR3?.KRelDelb, 'm') + tdC)
        builder.append(td + getZatez(oprij.KlOpR4?.KRelDelb, 'm') + tdC)
        builder.append(trC)

        builder.append(tableC)
    }

    private fun generateRelaySwitchAssignement(builder: StringBuilder) {
        builder.append(h2 + getString(R.string.relay_switching_assignment) + h2C)
        builder.append(table)
        builder.append(tr)
        builder.append(th + thC)
        builder.append(thcol4 + getString(R.string.relay_switching_assignment) + thC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)
        builder.append(trC)

        var x = 3
        for (i in 0..3) {
            builder.append(tr)
            builder.append(th + String.format(getString(R.string.relay_num_a), i + 1) + thC)
            builder.append(td + getPPRealoc(i, 1, mReallocs[i].rel_on.toInt()) + tdC)
            builder.append(td + getPPRealoc(i, 2, mReallocs[i].rel_on.toInt()) + tdC)
            builder.append(td + getPPRealoc(i, 3, mReallocs[i].rel_on.toInt()) + tdC)
            builder.append(td + getPPRealoc(i, 4, mReallocs[i].rel_on.toInt()) + tdC)
            builder.append(trC)

            x++

            builder.append(tr)
            builder.append(th + String.format(getString(R.string.relay_num_b), i + 1) + thC)
            builder.append(td + getPPRealoc(i, 1, mReallocs[i].rel_off.toInt()) + tdC)
            builder.append(td + getPPRealoc(i, 2, mReallocs[i].rel_off.toInt()) + tdC)
            builder.append(td + getPPRealoc(i, 3, mReallocs[i].rel_off.toInt()) + tdC)
            builder.append(td + getPPRealoc(i, 4, mReallocs[i].rel_off.toInt()) + tdC)
            builder.append(trC)

            x++
        }
        builder.append(tableC)
    }

    private fun getPPRealoc(i: Int, n: Int, y: Int): String
    ? {
        val z = y.toByte()
        val k = 2 * (4 - n)
        val msk = y shr k and 0x03
        var r = ""
        if (i + 1 == n) r = "/" else if (msk == 0x00) r = "" else if (msk == 0x01) r =
            "b" else if (msk == 0x02) r = "a" else if (msk == 0x03) r = ""
        return r
    }

    private fun getZatez(zz: Int?, NT: Char): String? {
        if (zz == null)
            return ""
        var zz = zz
        var r = ""
        if (NT == 'm') r = if (zz and 0xC00000 != 0) {
            getString(R.string.random)
        } else {
            getString(R.string.fixed)
        }
        if (NT == 't') {
            zz = zz and 0x0FFFFF
            r = String.format(
                "%02d:%02d:%02d",
                zz / 3600,
                zz % 3600 / 60,
                zz % 3600 % 60
            )
        }
        return r
    }

    private fun generateRelaySettings(builder: StringBuilder, oprij: Oprij) {
        builder.append(h2 + getString(R.string.relay_settings) + h2C)
        builder.append(table)
        builder.append(tr)
        builder.append(th + thC)
        for (i in 1..4)
            builder.append(th + String.format(getString(R.string.relay_num), i) + thC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.relay_installed) + thC)
        for (i in 0..3) {
            val mask = 0x80 shr i
            if ((oprij.VOpRe.StaPrij.toInt() and mask) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        }
        builder.append(trC)
        builder.append(tr)
        builder.append(th + getString(R.string.inverted_logic) + thC)
        for (i in 0..3) {
            val mask = 0x80 shr i
            if ((oprij.PolUKRe.toInt() and mask) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        }
        builder.append(trC)
        builder.append(tableC)
    }

    private fun generateGeneral(builder: StringBuilder) {
        builder.append(h2 + getString(R.string.general) + h2C)
        builder.append(table)

        val mTip = 1
        val version = Const.Data.CTipPrij[m_HWVerPri]

        builder.append(tr)
        builder.append(th + getString(R.string.device_type) + thC)
        builder.append(td + String.format("MTK-%d-%s-V-%d", mTip + 1, version, mSoftwareVersionPri) + tdC)
        builder.append(trC)

        if (fVis_RefPrij) {
            builder.append(tr)
            builder.append(th + getString(R.string.hdo_frequency) + thC)

            if (mSoftwareVersionPri >= 90) {
                if (mParFilteraCF.BROJ >= 0)
                    builder.append(td + String.format("%4.2f Hz", DataUtils.getTbparfiltera98mhz()[mParFilteraCF.BROJ].fre) + tdC)
            } else
                if (mParFiltera.BROJ >= 0)
                    if (mSoftwareVersionPri < 80)
                        builder.append(td + String.format("%4.2f Hz", DataUtils.tbParFiltera()[mParFiltera.BROJ].fre) + tdC)
                    else
                        builder.append(td + String.format("%4.2f Hz", DataUtils.getTbparfiltera98mhz()[mParFiltera.BROJ].fre) + tdC)

            builder.append(trC)

            builder.append(tr)
            builder.append(th + getString(R.string.raster) + thC)
            builder.append(td + getStringArray(R.array.rra, mBrojRast) + tdC)
            IsCZ44raster = mBrojRast == 4 || mBrojRast == 5
            IsCZRaster = mBrojRast in 4..7
            builder.append(trC)

            builder.append(tr)
            builder.append(th + getString(R.string.sensitivity) + thC)
            builder.append(td + String.format("%4.2f %%", mUtfPosto) + tdC)
            builder.append(trC)

            builder.append(tr)
            builder.append(th + getString(R.string.tel_raster_time_base) + thC)

            if ((mOp50Prij.RTCSinh.toInt() and 0x80) != 0)
                builder.append(td + getString(R.string.network50hz) + tdC)
            else
                builder.append(td + getString(R.string.clock) + tdC)
            builder.append(trC)
        }


        builder.append(tr)
        builder.append(th + getString(R.string.rtc_time_base) + thC)

        if ((mOp50Prij.RTCSinh.toInt() and 0x03) != 0)
            builder.append(td + getString(R.string.quartz) + tdC)
        else
            builder.append(td + getString(R.string.network50hz) + tdC)
        builder.append(trC)


        if (fVis_Cz96HDOBAT){
            builder.append(tr)
            builder.append(th + getString(R.string.rtc_dst) + thC)
            if(mOpPrij.PromjZLjU.toInt()!=0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.yes) + tdC)
            builder.append(trC)


            var rezim =-1
            var sta2=(mOpPrij.VOpRe.StaPrij.toInt() and 0x02)==0x2
            var sta1=(mOpPrij.VOpRe.StaPrij.toInt() and 0x01)==0x1
            if(!sta2 and sta1) rezim=1 // HDO =1 TIMESWITCH=0
            if(sta2 and sta1) rezim=0

            builder.append(tr)
            builder.append(th + getString(R.string.IDSI_REZIM) + thC)
            if(rezim==1)
                builder.append(td + getString(R.string.IDSI_REZIM_HDO) + tdC)
            else
                builder.append(td + getString(R.string.IDSI_REZIM_TIMESWITCH) + tdC)


        }



        if (!fVis_VersacomPS) {
            builder.append(tr)
            builder.append(th + getString(R.string.rtc_loss_action) + thC)

            var inx: Int =
                (mOp50Prij.RTCSinh.toInt() shr Const.Data.TIM_LOSS_RTC_POS and 0x0F)

            if (inx > 3)
                inx = 0
            builder.append(td + getStringArray(R.array.rtcloss, inx) + tdC)

            builder.append(trC)
        }

        if (fVis_RefPrij) {
            if (fVis_VersacomPS) {
                builder.append(tr)
                builder.append(th + getString(R.string.address_length_teleg) + thC)
                builder.append(td + String.format("%d", mOpPrij.VDuzAdr) + tdC)
                builder.append(trC)

                builder.append(tr)
                builder.append(th + getString(R.string.id) + thC)
                builder.append(td + String.format("%d", mOpPrij.VIdBr) + tdC)
                builder.append(trC)

            }

            if (!fVis_Cz95P) {
                builder.append(tr)
                builder.append(th + getString(R.string.sync_teleg_day) + thC)
                builder.append(td + String.format("%d", mOpPrij.VDuzAdr) + tdC)
                builder.append(trC)

                builder.append(tr)
                builder.append(th + getString(R.string.day_cycle_active) + thC)
                val dataStr = if ((mOpPrij.ParFlags.toInt() and 0x1) != 0) getString(R.string.yes) else getString(R.string.no)
                builder.append(td + dataStr + tdC)
                builder.append(trC)

                builder.append(tr)
                builder.append(th + getString(R.string.day_cycle_delay) + thC)

                val delay =
                    String.format("%02d:%02d", mOpPrij.Dly24H / 60, mOpPrij.Dly24H % 60)
                builder.append(td + delay + tdC)
                builder.append(trC)
            } else {
                builder.append(tr)
                builder.append(th + getString(R.string.track_relay_position) + thC)

                val dataStr =
                    if ((mOp50Prij.RTCSinh.toInt() and Const.Data.SINH_REL_POS_MASK) != 0)
                        getString(R.string.yes)
                    else getString(R.string.no)

                builder.append(td + dataStr + tdC)
                builder.append(trC)
            }
        }

        builder.append(tr)
        builder.append(th + getString(R.string.power_bridge_time) + thC)

        val timeBridge = (mOp50Prij.CPWBRTIME * 5.0 / 1000.0).toFloat()
        builder.append(td + String.format("%.2f s", timeBridge) + tdC)
        builder.append(trC)

        var PARAMSRC_FILE =0
        var PARAMSRC_DEVICE =1
        var PARAMSRC_NEW =2
        var m_paramSrc=PARAMSRC_DEVICE
        builder.append(tr+ thcol2bgth +getString(R.string.IDSI_PARAMS)+thC+trC)


        if(m_paramSrc==PARAMSRC_DEVICE)
        {
            builder.append(tr+th+getString(R.string.IDSI_LASTREPARAMTIME)+thC+td+pLastPa.DataTimeS+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_CREATED_UID)+thC+td+pLastPa.IDCreateS+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_CREATED_PCUID)+thC+td+pLastPa.CreateSiteS+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_REPARM_UID)+thC+td+pLastPa.IDReParaS+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_REPARM_PCUID)+thC+td+pLastPa.ReParaSiteS+tdC+trC)

            builder.append(tr+th+getString(R.string.IDSI_PARM_FILE)+thC+td+pLastPa.IDFileS+".mtk"+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_SERIALNUM)+thC+td+m_dwDeviceSerNr+tdC+trC)
        }
        else if(m_paramSrc==PARAMSRC_FILE)
        {
            builder.append(tr+th+getString(R.string.IDSI_CREATED_UID)+thC+td+pLastPa.IDCreateS+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_CREATED_PCUID)+thC+td+pLastPa.CreateSiteS+tdC+trC)
            builder.append(tr+th+getString(R.string.IDSI_PARM_FILE)+thC+td+pLastPa.IDFileS+".mtk"+tdC+trC)

            builder.append(tr+th+getString(R.string.IDSI_COMMENT)+thC+td+"Comment"+tdC+trC) //TODO add comment iz fajla

        }
        else if(m_paramSrc==PARAMSRC_NEW)
        {
            builder.append(tr+ td +getString(R.string.IDSI_PARM_FROM_NEW)+thC+trC)
        }

        builder.append(tableC)
    }

    private fun getStringArray(resId: Int, index: Int): String {
        return context.resources.getStringArray(resId)[index]
    }

    private fun getString(resId: Int): String {
        return context.resources.getString(resId)
    }

    private fun getFriRPar(
        dbuf: ByteArray,
        mParFilteraCf: StrParFilVer9,
        mParFiltera: StrParFil
    ) {
        if (mSoftwareVersionPri >= 90) {
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
                mBrojRast = setOprelI(dbuf)
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
            mBrojRast = setOprelI(dbuf)
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
                mUtfPosto = (utth * UTFREFP) / uthMinRef
            } else
                mUtfPosto = 0.0
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
        if (mSoftwareVersionPri >= 96)
            getTlg50ParV96(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnd)
    }

    private fun getTlg50ParV96(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mTelegSync: List<Telegram>,
        mTlgFnd: List<Telegram>
    ) {
        // if (m_CFG.cID >= 0x8C) {
        getTlgID50ParV96(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnd)
        //} else {
        //    when (mgaddr.objectt) {
        //        0 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel1)
        //        1 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel2)
        //        2 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel3)
        //        3 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel4)
        //    }
        //}
    }

    private fun getTlgID50ParV96(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mTelegSync: List<Telegram>,
        mTlgFnd: List<Telegram>
    ) {
        when (mgaddr.objectt) {
            0 -> storeDataTlgRel(
                dbuf,
                mOp50Prij.TlgRel1
            )  //TODO iskoristit istu funkciju storeDataTlgFn
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
            0 -> if (mSoftwareVersionPri >= 96)
                getKlDatVer96(dbuf, mOp50Prij, mOpPrij, mReallocs)

            1 -> if (mSoftwareVersionPri >= 96)
                getKl2VerDatVer96(dbuf, mOp50Prij, mOpPrij)

            2 -> getDaljPar(dbuf, mOpPrij)
        }
    }

    private fun getDaljPar(dbuf: ByteArray, mOpPrij: Oprij) {
        mOpPrij.VOpRe.VakProR1 = setOprelI(dbuf)
        mOpPrij.VOpRe.VakProR2 = setOprelI(dbuf)
        mOpPrij.VOpRe.VakProR3 = setOprelI(dbuf)
        mOpPrij.VOpRe.VakProR4 = setOprelI(dbuf)

        if (mSoftwareVersionPri < 98) mOpPrij.VOpRe.StaPrij=dbuf[globalIndex++]
        if(mSoftwareVersionPri < 95)
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

        if ((m_CFG.cID == 100) || (m_CFG.cID == 120))
            inCik = true

        if (inCik)
            mOpPrij.VDuzAdr = dbuf[globalIndex++]

        mOpPrij.PolUKRe = dbuf[globalIndex++]

        if (m_CFG.cID == 100)
            mOpPrij.VIdBr = dbuf[globalIndex++]

        mOp50Prij.RTCSinh = dbuf[globalIndex++]

        if (inCik) mOp50Prij.WDaySinh = dbuf[globalIndex++]
        else mOp50Prij.WDaySinh = 0

        if (m_CFG.cID == 100) {
            globalIndex++
            globalIndex++
            globalIndex++
            globalIndex++
        } else if (m_CFG.cID == 130 || m_CFG.cID == 0x8C) {
            mOpPrij.VOpRe.StaPrij =dbuf[globalIndex++]
            mOpPrij.PromjZLjU =dbuf[globalIndex++]
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


        if (m_CFG.cID == 120 || m_HWVerPri == Const.Data.TIP_PS || m_HWVerPri == Const.Data.TIP_PSB)
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

    private fun getTparPar(dbuf: ByteArray,oPProg:Opprog)
            : Opprog {
        //val oPProg = Opprog()


        val x = Unitimbyt()
        var nrTpar = 8

        val TIP_SPA = 2
        val NR_TPAR_SPA = 5
        val NR_TPAR_MAX = 14


        nrTpar = if (mSoftwareVersionPri >= 90 && m_HWVerPri == TIP_SPA)
            NR_TPAR_SPA
        else if (mSoftwareVersionPri in 40..95)
            NR_TPAR_MAX
        else
            m_CFG.cNpar.toInt()


        x.b[1] = dbuf[globalIndex++]
        if (mSoftwareVersionPri >= 40)x.b[0] = dbuf[globalIndex++]


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
                    mSoftwareVersionPri = (char - '0') * 10
                char = headString.get(indexOfVersionStart + 3)
                if (char.isDigit())
                    mSoftwareVersionPri += (char - '0')
                break
            }
        }

        val startIndexOfParams = headString.indexOf(";")

        val buff = strCopyHexToBuf(headString, startIndexOfParams + 1)
        m_CFG.cBrparam = buff[0]
        val a: Int = buff[1].toInt() and 0xFF
        val b: Int = buff[2].toInt() and 0xFF
        m_CFG.cID = 256 * a + b

        m_CFG.cPcbRev = buff[3]
        m_CFG.cNrel = buff[4]
        m_CFG.cRtc = buff[5]
        m_CFG.cNprog = buff[6]
        m_CFG.cNpar = buff[7]
    }

    // HELPER METHODS

    private fun strCopyHexToBuf(headString: String, index: Int)
            : List<Byte> {
        val buf = mutableListOf<Byte>()

        val len = (headString.length - index) / 2
        var i = 0

        var nIndex = index

        var lb: Byte
        var hb: Byte

        while (i++ < len) {
            hb = headString[nIndex++].toByte()
            lb = headString[nIndex++].toByte()

            if (hb == ')'.toByte() || lb == ')'.toByte())
                break

            if (hb == '\r'.toByte() || lb == '\r'.toByte())
                break

            hb = HextoD(hb, lb)
            buf.add(hb)
        }
        return buf
    }

    private fun HtoB(ch: Char) : Char? {
        if (ch in '0'..'9') return (ch - '0').toChar()
        if (ch in 'A'..'F') return (ch - 'A' + 0xA).toChar()
        return null
    }

    private fun HextoD(hb: Byte, lb: Byte)
            : Byte {
        var mb: Byte
        mb = 0
        if (hb >= '0'.toByte() && hb <= '9'.toByte() || hb >= 'A'.toByte() && hb <= 'F'.toByte() ||
            lb >= '0'.toByte() && lb <= '9'.toByte() || lb >= 'A'.toByte() && lb <= 'F'.toByte()
        ) {
            mb =
                if (hb >= 'A'.toByte()) ((hb - '7'.toByte()) * 16).toByte() else ((hb - '0'.toByte()) * 16).toByte()
            mb =
                if (lb >= 'A'.toByte()) (mb + (lb - '7'.toByte())).toByte() else (mb + (lb - '0'.toByte())).toByte()
        }
        return mb
    }

}


