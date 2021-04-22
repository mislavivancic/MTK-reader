package com.mtkreader.services

import android.content.Context
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.data.DataStructures
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



class DisplayServiceImpl : DisplayDataContract.DisplayService, KoinComponent {

    val context: Context by inject()

    private var data: DataStructures = DataStructures()

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

    private var m_SWVerPri = 0
    private var m_HWVerPri = 0

    private var cntWork = 0

    private fun setupFlags() {
        m_HWVerPri=data.mHardwareVersion
        m_SWVerPri=data.mSoftwareVersion


        fVis_VersacomPS = (m_HWVerPri != Const.Data.TIP_PS) && (m_HWVerPri != Const.Data.TIP_PSB)
        fVis_Versacom = m_HWVerPri != Const.Data.TIP_S && m_HWVerPri != Const.Data.TIP_SN && m_HWVerPri != Const.Data.TIP_SPN
        fVis_Uklsat =
            m_HWVerPri == Const.Data.TIP_SPA || m_HWVerPri == Const.Data.TIP_S || m_HWVerPri == Const.Data.TIP_SN || m_HWVerPri == Const.Data.TIP_SPN
        fVis_Prazdani =
            m_SWVerPri >= 94 && (m_HWVerPri == Const.Data.TIP_S || m_HWVerPri == Const.Data.TIP_SN || m_HWVerPri == Const.Data.TIP_SPN)
        fVis_Sezone =
            (fVis_Versacom && m_HWVerPri != Const.Data.TIP_PA) || (fVis_Uklsat && m_SWVerPri >= 95)
        fVis_Sezone =
            fVis_Sezone && m_SWVerPri >= 80 && m_HWVerPri != Const.Data.TIP_PS && (m_HWVerPri != Const.Data.TIP_PSB)
        fVis_Asat = m_HWVerPri == Const.Data.TIP_PASN || m_HWVerPri == Const.Data.TIP_SN || m_HWVerPri == Const.Data.TIP_SPN
        fVis_RefPrij = m_HWVerPri != Const.Data.TIP_S && m_HWVerPri != Const.Data.TIP_SN
        fVis_TBAS = m_HWVerPri != Const.Data.TIP_PA
        fVis_DUZADR = m_HWVerPri != Const.Data.TIP_SPN
        fVis_Realoc = m_SWVerPri >= 82

        fVis_Cz95P = m_SWVerPri >= 95
        fVis_Cz96P = m_SWVerPri >= 96
        fVis_Cz96HDOBAT = m_HWVerPri == Const.Data.TIP_PSB

    }

    override fun generateHtml(dataStructures: DataStructures): String {
       data=dataStructures

        val htmlBuilder = StringBuilder()
        htmlBuilder.append(Css.htmlstart)
        val title="Naslov"
        htmlBuilder.append("<title>"+ title+"</title>")
        htmlBuilder.append("<style>"+ getString(R.string.css)+"</style>")
        htmlBuilder.append("</head>")
        htmlBuilder.append(body)
        htmlBuilder.append("<div class=\"container\">")
        setupFlags()

        generateGeneral(htmlBuilder)


        generateRelaySettings(htmlBuilder, data.mOprij)

        if (fVis_Realoc)
            generateRelaySwitchAssignement(htmlBuilder)

        if (fVis_RefPrij) {
            generateSwitchingDelay(htmlBuilder, data.mOprij)
            generateClassicTelegram(htmlBuilder)
        }

        if (fVis_Cz96P) {
            generateAdditionalTelegram(htmlBuilder)
        }

        if (fVis_RefPrij && fVis_Cz95P) {
            generateTelegramSync(htmlBuilder)
            //generateSyncTelegramDoW(builder)
        }

        generateWorkSchedules(htmlBuilder,  data.mOprij)
        generateWiperAndClosedLoop(htmlBuilder, data.mWipersRx.toList())
        generateLearnFunctions(htmlBuilder, data.mLearningRx.toList())
        generateTalegramAbsence(htmlBuilder, data.mTelegAbsenceRx.toList())
        generateArrivalAndLossOfSupply(htmlBuilder, data.mPonPoffRx.toList())
        generateEventLog(htmlBuilder)

        generateInterlock(htmlBuilder)



        htmlBuilder.append("</div>${bodyC}${htmlC}")

        return htmlBuilder.toString()
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
        val cfg = data.mRelInterlock[rel].PcCnfg[0]// ?? m_RELINTERLOCK je velicine 8 na PCU
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
    val mLogEnFlags = arrayOf(data.mOp50rij.CLOGENFLGS[0].toInt(), data.mOp50rij.CLOGENFLGS[1].toInt())
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
            builder.append("${td}/${tdC}")
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
                0 -> buildersWorkSchedTimePairs.add(showTimePairs(data.mPProgR1))
                1 -> buildersWorkSchedTimePairs.add(showTimePairs(data.mPProgR2))
                2 -> buildersWorkSchedTimePairs.add(showTimePairs(data.mPProgR3))
                3 -> buildersWorkSchedTimePairs.add(showTimePairs(data.mPProgR4))
            }
        }
        val buildersWorkSchedTimeDays = mutableListOf<StringBuilder>()

        for (relay in 0..3) {
            if ((oprij.VOpRe.StaPrij.toInt() and (0x80 shr relay)) == 0)continue
            when (relay) {
                0 -> buildersWorkSchedTimeDays.add(getRelAkProg(data.mPProgR1))
                1 -> buildersWorkSchedTimeDays.add(getRelAkProg(data.mPProgR2))
                2 -> buildersWorkSchedTimeDays.add(getRelAkProg(data.mPProgR3))
                3 -> buildersWorkSchedTimeDays.add(getRelAkProg(data.mPProgR4))
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
            val yesNo = if ((DataUtils.getIVtmask(pItem) and data.mOprij.VOpRe.VakProR1) != 0)
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
    for (itemIndex in 0..data.mCfg.cNpar) {
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

            var Tm=""

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
        builder.append(th +"DBQ:GetTlgNameByContent"+ thC)
        builder.append(th + getSyncTime(data.mOp50rij.SinhTime[i], m_HWVerPri) + thC)
        getRasterString(builder, data.mTelegSync[i].Cmd)
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

    val TGFN= arrayOf( R.string.IDSCB_FN_TELEG1_0 ,R.string.IDSCB_FN_TELEG1_6 ,R.string.IDSCB_FN_TELEG1_7 ,R.string.IDSCB_FN_TELEG1_18 ,R.string.IDSCB_FN_TELEG1_8 ,R.string.IDSCB_FN_TELEG1_9 ,R.string.IDSCB_FN_TELEG1_10 ,R.string.IDSCB_FN_TELEG1_11 ,R.string.IDSCB_FN_TELEG1_12 ,R.string.IDSCB_FN_TELEG1_13 ,R.string.IDSCB_FN_TELEG1_14 ,R.string.IDSCB_FN_TELEG1_15 ,R.string.IDSCB_FN_TELEG1_16 ,R.string.IDSCB_FN_TELEG1_17)

    if(fVis_Cz96P) {
        for(i in 0..7)
        {
            builder.append(tr)
            builder.append(th +"DBQ:GetTlgNameByContent"+ thC)
            val f:Int=data.mOp50rij.tlg[i].tel1.Cmd.Fn.toInt()

            if (i in 0..6)
                builder.append(th +getString(TGFN[f])+ thC)

            if(i==7){
                if(f==0x40) builder.append(th +getString(R.string.IDSI_EMERGTLG)+ " '" +getString(R.string.IDSI_OFF)+"'"+ thC) //AKT_SET_OFF
                if(f==0x80) builder.append(th +getString(R.string.IDSI_EMERGTLG)+ " '" +getString(R.string.IDSI_ON)+"'"+ thC) //AKT_SET_ON
            }

            getRasterString(builder, data.mOp50rij.tlg[i].tel1.Cmd)
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

    GetRelTlgs(builder,data.mOp50rij.TlgRel1,1)
    GetRelTlgs(builder,data.mOp50rij.TlgRel2,2)
    GetRelTlgs(builder,data.mOp50rij.TlgRel3,3)
    GetRelTlgs(builder,data.mOp50rij.TlgRel4,4)

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
    var str=""
    var tstr: String
    var dstr: String
    var stime: Int
    var tmpi: Int
    if (ver == Const.Data.TIP_PA) {
        stime = t and 0x000FFFFF
        str = String.format(
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
        tstr = String.format("%02d:%02d:%02d", rtctime.b[2].toInt() and 0x1F, rtctime.b[1].toInt(), rtctime.b[0].toInt())
        var b3:Int=rtctime.b[3].toInt()

        var sday=b3 and 0x07
        if(sday==0) sday=7 else sday=sday-1
        dstr=getStringArray(R.array.dan_sync_tg, sday % 8)
        if( ( b3 and 0x40) >0 ) //samo dan
        {
            str = if(sday==7) "---" else dstr
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
        builder.append(thrw3 + s1 + thC)
        builder.append(thrw3 + s2 + thC)
        GetRasterHeadStringABDPC(builder)
        builder.append(trC)
        builder.append(tr);GetRasterHeadStringNUM(builder,false);builder.append(trC)
        builder.append(tr);GetRasterHeadStringNUM(builder,true);builder.append(trC)
    }
    else{
        builder.append(tr);GetRasterHeadStringNUM(builder,false);builder.append(trC)
        builder.append(tr + trC)
        builder.append(tr + trC)
    }

}

private fun GetRelTlgs(builder: StringBuilder, T: Telegrel, index:Int) {
    builder.append(tr)
    builder.append(thrw2 +"DBQ:GetTlgNameByContentK"+ thC)
    builder.append(th +"R"+index+getString(R.string.IDSI_ON)+ thC)
    getRasterString(builder,T.Uk)
    builder.append(trC)

    builder.append(tr)
    builder.append(th +"R"+index+getString(R.string.IDSI_OFF)+ thC)
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
    builder.append(td + getZatez(oprij.KlOpR1.KRelDela, 't') + tdC)
    builder.append(td + getZatez(oprij.KlOpR2.KRelDela, 't') + tdC)
    builder.append(td + getZatez(oprij.KlOpR3.KRelDela, 't') + tdC)
    builder.append(td + getZatez(oprij.KlOpR4.KRelDela, 't') + tdC)
    builder.append(trC)

    builder.append(tr)
    builder.append(th + getString(R.string.delay_a) + thC)
    builder.append(td + getZatez(oprij.KlOpR1.KRelDela, 'm') + tdC)
    builder.append(td + getZatez(oprij.KlOpR2.KRelDela, 'm') + tdC)
    builder.append(td + getZatez(oprij.KlOpR3.KRelDela, 'm') + tdC)
    builder.append(td + getZatez(oprij.KlOpR4.KRelDela, 'm') + tdC)
    builder.append(trC)

    builder.append(tr)
    builder.append(th + getString(R.string.delay_b) + thC)
    builder.append(td + getZatez(oprij.KlOpR1.KRelDelb, 't') + tdC)
    builder.append(td + getZatez(oprij.KlOpR2.KRelDelb, 't') + tdC)
    builder.append(td + getZatez(oprij.KlOpR3.KRelDelb, 't') + tdC)
    builder.append(td + getZatez(oprij.KlOpR4.KRelDelb, 't') + tdC)
    builder.append(trC)

    builder.append(tr)
    builder.append(th + getString(R.string.delay_b) + thC)
    builder.append(td + getZatez(oprij.KlOpR1.KRelDelb, 'm') + tdC)
    builder.append(td + getZatez(oprij.KlOpR2.KRelDelb, 'm') + tdC)
    builder.append(td + getZatez(oprij.KlOpR3.KRelDelb, 'm') + tdC)
    builder.append(td + getZatez(oprij.KlOpR4.KRelDelb, 'm') + tdC)
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
        builder.append(td + getPPRealoc(i, 1, data.mRealloc[i].rel_on.toInt()) + tdC)
        builder.append(td + getPPRealoc(i, 2, data.mRealloc[i].rel_on.toInt()) + tdC)
        builder.append(td + getPPRealoc(i, 3, data.mRealloc[i].rel_on.toInt()) + tdC)
        builder.append(td + getPPRealoc(i, 4, data.mRealloc[i].rel_on.toInt()) + tdC)
        builder.append(trC)

        x++

        builder.append(tr)
        builder.append(th + String.format(getString(R.string.relay_num_b), i + 1) + thC)
        builder.append(td + getPPRealoc(i, 1, data.mRealloc[i].rel_off.toInt()) + tdC)
        builder.append(td + getPPRealoc(i, 2, data.mRealloc[i].rel_off.toInt()) + tdC)
        builder.append(td + getPPRealoc(i, 3, data.mRealloc[i].rel_off.toInt()) + tdC)
        builder.append(td + getPPRealoc(i, 4, data.mRealloc[i].rel_off.toInt()) + tdC)
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
    when {
        i + 1 == n -> r = "/"
        msk == 0x00 -> r = ""
        msk == 0x01 -> r = "b"
        msk == 0x02 -> r = "a"
        msk == 0x03 -> r = ""
    }
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
    builder.append(td + String.format("MTK-%d-%s-V-%d", mTip + 1, version, m_SWVerPri) + tdC)
    builder.append(trC)

    if (fVis_RefPrij) {
        builder.append(tr)
        builder.append(th + getString(R.string.hdo_frequency) + thC)

        if (m_SWVerPri >= 90) {
            if (data.mParFilteraCF.BROJ >= 0)
                builder.append(td + String.format("%4.2f Hz", DataUtils.getTbparfiltera98mhz()[data.mParFilteraCF.BROJ].fre) + tdC)
        } else
            if (data.mParFiltera.BROJ >= 0)
                if (m_SWVerPri < 80)
                    builder.append(td + String.format("%4.2f Hz", DataUtils.tbParFiltera()[data.mParFiltera.BROJ].fre) + tdC)
                else
                    builder.append(td + String.format("%4.2f Hz", DataUtils.getTbparfiltera98mhz()[data.mParFiltera.BROJ].fre) + tdC)

        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.raster) + thC)
        builder.append(td + getStringArray(R.array.rra, data.mBrojRast) + tdC)
        IsCZ44raster = data.mBrojRast == 4 || data.mBrojRast == 5
        IsCZRaster = data.mBrojRast in 4..7
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.sensitivity) + thC)
        builder.append(td + String.format("%4.2f %%", data.mUtfPosto) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.tel_raster_time_base) + thC)

        if ((data.mOp50rij.RTCSinh.toInt() and 0x80) != 0)
            builder.append(td + getString(R.string.network50hz) + tdC)
        else
            builder.append(td + getString(R.string.clock) + tdC)
        builder.append(trC)
    }


    builder.append(tr)
    builder.append(th + getString(R.string.rtc_time_base) + thC)

    if ((data.mOp50rij.RTCSinh.toInt() and 0x03) != 0)
        builder.append(td + getString(R.string.quartz) + tdC)
    else
        builder.append(td + getString(R.string.network50hz) + tdC)
    builder.append(trC)


    if (fVis_Cz96HDOBAT){
        builder.append(tr)
        builder.append(th + getString(R.string.rtc_dst) + thC)
        if(data.mOprij.PromjZLjU.toInt()!=0)
            builder.append(td + getString(R.string.yes) + tdC)
        else
            builder.append(td + getString(R.string.yes) + tdC)
        builder.append(trC)


        var rezim =-1
        val sta2=(data.mOprij.VOpRe.StaPrij.toInt() and 0x02)==0x2
        val sta1=(data.mOprij.VOpRe.StaPrij.toInt() and 0x01)==0x1
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
            (data.mOp50rij.RTCSinh.toInt() shr Const.Data.TIM_LOSS_RTC_POS and 0x0F)

        if (inx > 3)
            inx = 0
        builder.append(td + getStringArray(R.array.rtcloss, inx) + tdC)

        builder.append(trC)
    }

    if (fVis_RefPrij) {
        if (fVis_VersacomPS) {
            builder.append(tr)
            builder.append(th + getString(R.string.address_length_teleg) + thC)
            builder.append(td + String.format("%d", data.mOprij.VDuzAdr) + tdC)
            builder.append(trC)

            builder.append(tr)
            builder.append(th + getString(R.string.id) + thC)
            builder.append(td + String.format("%d", data.mOprij.VIdBr) + tdC)
            builder.append(trC)

        }

        if (!fVis_Cz95P) {
            builder.append(tr)
            builder.append(th + getString(R.string.sync_teleg_day) + thC)
            builder.append(td + String.format("%d", data.mOprij.VDuzAdr) + tdC)
            builder.append(trC)

            builder.append(tr)
            builder.append(th + getString(R.string.day_cycle_active) + thC)
            val dataStr = if ((data.mOprij.ParFlags.toInt() and 0x1) != 0) getString(R.string.yes) else getString(R.string.no)
            builder.append(td + dataStr + tdC)
            builder.append(trC)

            builder.append(tr)
            builder.append(th + getString(R.string.day_cycle_delay) + thC)

            val delay =
                String.format("%02d:%02d", data.mOprij.Dly24H / 60, data.mOprij.Dly24H % 60)
            builder.append(td + delay + tdC)
            builder.append(trC)
        } else {
            builder.append(tr)
            builder.append(th + getString(R.string.track_relay_position) + thC)

            val dataStr =
                if ((data.mOp50rij.RTCSinh.toInt() and Const.Data.SINH_REL_POS_MASK) != 0)
                    getString(R.string.yes)
                else getString(R.string.no)

            builder.append(td + dataStr + tdC)
            builder.append(trC)
        }
    }

    builder.append(tr)
    builder.append(th + getString(R.string.power_bridge_time) + thC)

    val timeBridge = (data.mOp50rij.CPWBRTIME * 5.0 / 1000.0).toFloat()
    builder.append(td + String.format("%.2f s", timeBridge) + tdC)
    builder.append(trC)



    builder.append(tr + thcol2bgth +getString(R.string.IDSI_PARAMS)+ thC + trC)
    when (data.m_paramSrc) {
        Const.PARAMSRC.DEVICE -> {
            builder.append(tr + th +getString(R.string.IDSI_LASTREPARAMTIME)+ thC + td +data.m_cLastParData.DataTimeS+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_CREATED_UID)+ thC + td +data.m_cLastParData.IDCreateS+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_CREATED_PCUID)+ thC + td +data.m_cLastParData.CreateSiteS+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_REPARM_UID)+ thC + td +data.m_cLastParData.IDReParaS+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_REPARM_PCUID)+ thC + td +data.m_cLastParData.ReParaSiteS+ tdC + trC)

            builder.append(tr + th +getString(R.string.IDSI_PARM_FILE)+ thC + td +data.m_cLastParData.IDFileS+".mtk"+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_SERIALNUM)+ thC + td +data.m_dwDeviceSerNr+ tdC + trC)
        }
        Const.PARAMSRC.FILE -> {
            builder.append(tr + th +getString(R.string.IDSI_CREATED_UID)+ thC + td +data.mCFileParData.IDCreateS+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_CREATED_PCUID)+ thC + td +data.mCFileParData.CreateSiteS+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_PARM_FILE)+ thC + td +data.mCFileParData.IDFileS+".mtk"+ tdC + trC)
            builder.append(tr + th +getString(R.string.IDSI_COMMENT)+ thC + td +data.mFileComment+ tdC + trC)

        }
        Const.PARAMSRC.NEW -> {
            builder.append(tr + td +getString(R.string.IDSI_PARM_FROM_NEW)+ thC + trC)
        }
    }

    builder.append(tableC)
}

private fun getStringArray(resId: Int, index: Int): String {
    return context.resources.getStringArray(resId)[index]
}

private fun getString(resId: Int): String {
    return context.resources.getString(resId)
}
}