package com.mtkreader.services

import android.content.Context
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.TIP_PA
import com.mtkreader.commons.Const.Data.TIP_PASN
import com.mtkreader.commons.Const.Data.TIP_PS
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
import com.mtkreader.utils.HtmlTags.thcol4
import com.mtkreader.utils.HtmlTags.thcol8
import com.mtkreader.utils.HtmlTags.tr
import com.mtkreader.utils.HtmlTags.trC
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.experimental.or
import kotlin.math.pow

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

    var IsCZRaster = false
    var IsCZ44raster = false


    // data to be filled
    private lateinit var wipers: List<Wiper>
    private lateinit var pOnPOffRDat: List<PonPoffStr>
    private lateinit var tlgAbsenceDat: List<TlgAbstr>
    private lateinit var learningData: List<StrLoadMng>
    private lateinit var mRelInterLock: List<IntrlockStr>
    private val mPProgR1 = mutableListOf<Opprog>()
    private val mPProgR2 = mutableListOf<Opprog>()
    private val mPProgR3 = mutableListOf<Opprog>()
    private val mPProgR4 = mutableListOf<Opprog>()
    private val mOpPrij = Oprij()
    private val mOp50Prij = Oprij50()
    private val mReallocs = mutableListOf<Rreallc>()
    private val mTelegSync = mutableListOf<Telegram>()
    private val mTlgFnD = mutableListOf<Telegram>()
    private val mParFilteraCF = StrParFilVer9()
    private val mParFiltera = StrParFil()


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

        while (hasNextLine(data)) {
            if (mline[0] != Const.Tokens.END_TOKEN.toByte())
                getLineData()
            else
                break
        }

        setupFlags()
        return generateHtml(
            wipers,
            pOnPOffRDat,
            tlgAbsenceDat,
            learningData,
            mPProgR1,
            mPProgR2,
            mPProgR3,
            mPProgR4,
            mOpPrij
        )
    }

    private fun setupFlags() {
        fVis_VersacomPS = m_HWVerPri != TIP_PS
        fVis_VersacomPS = m_HWVerPri != TIP_PS
        fVis_Versacom = m_HWVerPri != TIP_S && m_HWVerPri != TIP_SN && m_HWVerPri != TIP_SPN
        fVis_Uklsat =
            m_HWVerPri == TIP_SPA || m_HWVerPri == TIP_S || m_HWVerPri == TIP_SN || m_HWVerPri == TIP_SPN
        fVis_Prazdani =
            mSoftwareVersionPri >= 94 && (m_HWVerPri == TIP_S || m_HWVerPri == TIP_SN || m_HWVerPri == TIP_SPN)
        fVis_Sezone =
            fVis_Versacom && m_HWVerPri != TIP_PA || fVis_Uklsat && mSoftwareVersionPri >= 95
        fVis_Sezone = fVis_Sezone && mSoftwareVersionPri >= 80 && m_HWVerPri != TIP_PS
        fVis_Asat = m_HWVerPri == TIP_PASN || m_HWVerPri == TIP_SN || m_HWVerPri == TIP_SPN
        fVis_RefPrij = m_HWVerPri != TIP_S && m_HWVerPri != TIP_SN
        fVis_TBAS = m_HWVerPri != TIP_PA
        fVis_DUZADR = m_HWVerPri != TIP_SPN
        fVis_Realoc = mSoftwareVersionPri >= 82

        fVis_Cz95P = mSoftwareVersionPri >= 95
        fVis_Cz96P = mSoftwareVersionPri >= 96
    }

    private fun getLineData() {
        var i = 0
        val m_gaddr = Mgaddr(0)
        var bb: Char

        while (i < 5) {
            if (mline[i] == '('.toByte())
                break
            bb = HtoB(mline[i++].toChar())
            if (isCheck) {
                m_gaddr.i = m_gaddr.i shl 4
                m_gaddr.i = m_gaddr.i or bb.toInt()
            } else
                m_Dateerr++
        }
        m_gaddr.update()


        if (mline[i] != ')'.toByte())
            m_Dateerr++

        i = 5
        val dbuf = ByteArray(128)


        for (j in 0..128) {
            var k = 2
            while (k != 0) {
                k--
                if (mline[i] == ')'.toByte())
                    break
                bb = HtoB(mline[i++].toChar())
                if (isCheck) {
                    dbuf[j] = (dbuf[j] * (2f.pow(4)).toInt()).toByte()
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
        globalIndex = 0

        when (mgaddr.group) {

            in 1..4 -> {
                globalIndex = 0
                val oPProg = getTparPar(dbuf)
                when (mgaddr.group) {
                    1 -> mPProgR1.add(mgaddr.objectt, oPProg)
                    2 -> mPProgR2.add(mgaddr.objectt, oPProg)
                    3 -> {
                        mRelInterLock = getRelInterLock(dbuf)
                        globalIndex = 0
                        mPProgR3.add(mgaddr.objectt, oPProg)
                    }
                    4 -> mPProgR4.add(mgaddr.objectt, oPProg)
                }
            }
            5 -> {
                when (mgaddr.objectt) {
                    0 -> wipers = getWipers(dbuf)
                    1 -> pOnPOffRDat = getPonPoffRDat(dbuf)
                    2 -> tlgAbsenceDat = getTlgAbsenceDat(dbuf)
                    3 -> learningData = getLearningDat(dbuf)
                }
            }


            8 -> getOprijParV9(mgaddr, dbuf, mOp50Prij, mOpPrij, mReallocs)
            9 -> getTlg50Par(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnD)
            12 -> getFriRPar(dbuf, mParFilteraCF, mParFiltera)
        }

    }

    private fun initData() {
        for (i in 0..15) {
            mPProgR1.add(Opprog())
            mPProgR2.add(Opprog())
            mPProgR3.add(Opprog())
            mPProgR4.add(Opprog())
        }
        for (i in 0..4)
            mTelegSync.add(Telegram())

        for (i in 0..7)
            mTlgFnD.add(Telegram())

    }

    private fun generateHtml(
        wipers: List<Wiper>,
        ponPoffstrs: List<PonPoffStr>,
        tlgAbstrs: List<TlgAbstr>,
        strLoadMngs: List<StrLoadMng>,
        mPProgR1: List<Opprog>,
        mPProgR2: List<Opprog>,
        mPProgR3: List<Opprog>,
        mPProgR4: List<Opprog>,
        oprij: Oprij
    ): String {
        val htmlBuilder = StringBuilder()
        htmlBuilder.append(Css.css)
        htmlBuilder.append(body)
        generateContent(
            htmlBuilder,
            wipers,
            ponPoffstrs,
            tlgAbstrs,
            strLoadMngs,
            mPProgR1,
            mPProgR2,
            mPProgR3,
            mPProgR4,
            oprij
        )
        htmlBuilder.append("$bodyC$htmlC")

        return htmlBuilder.toString()
    }

    private fun generateContent(
        builder: java.lang.StringBuilder,
        wipers: List<Wiper>,
        ponPoffstrs: List<PonPoffStr>,
        tlgAbstrs: List<TlgAbstr>,
        strLoadMngs: List<StrLoadMng>,
        mPProgR1: List<Opprog>,
        mPProgR2: List<Opprog>,
        mPProgR3: List<Opprog>,
        mPProgR4: List<Opprog>,
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
            generateSyncTelegramDoW(builder)
        }

        generateWorkSchedules(mPProgR1, mPProgR2, mPProgR3, mPProgR4, oprij, builder)
        generateWiperAndClosedLoop(builder, wipers)
        generateLearnFunctions(builder, strLoadMngs)

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
            if ((strLoadMngs[i].status2.toInt() and Const.Data.LEARN_7DAYS_MASK) == 0)
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
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)

        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.retrigerable) + thC)
        for (i in 0..3)
            if ((wipers[i].status.toInt() and Const.Data.WIPPER_RETRIG_MASK) != 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)



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
            builder.append(td + getHMSfromInt(0) + tdC)

        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.scheduled_switching_activation_delay) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(wipers[i].TBlockPrePro) + tdC)

        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.loop_enable) + thC)
        for (i in 0..3)
            if ((wipers[i].status.toInt() and Const.Data.LOOP_DISEB_MASK) == 0)
                builder.append(td + getString(R.string.yes) + tdC)
            else
                builder.append(td + getString(R.string.no) + tdC)
        builder.append(trC)

        builder.append(tr)
        builder.append(th + getString(R.string.duration_in_position) + thC)
        for (i in 0..3)
            builder.append(td + getHMSfromInt(wipers[i].TWiper) + tdC)
        builder.append(trC)

        builder.append(tableC)
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
        mPProgR1: List<Opprog>,
        mPProgR2: List<Opprog>,
        mPProgR3: List<Opprog>,
        mPProgR4: List<Opprog>,
        oprij: Oprij,
        builder: java.lang.StringBuilder
    ) {
        val buildersWorkSchedTimePairs = mutableListOf<StringBuilder>()
        if (fVis_Versacom)
            for (relay in 0..3)
                when (relay) {
                    0 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR1))
                    1 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR2))
                    2 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR3))
                    3 -> buildersWorkSchedTimePairs.add(showTimePairs(mPProgR4))
                }

        val buildersWorkSchedTimeDays = mutableListOf<StringBuilder>()

        for (relay in 0..3) {
            if (oprij.VOpRe.StaPrij.toInt() and (0x80 shr relay) == 0)
                continue
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


    private fun getRelAkProg(mPProgR: List<Opprog>): StringBuilder {
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


    private fun showTimePairs(mPProgR: List<Opprog>): StringBuilder {
        val timePairsTableBuilder = StringBuilder()
        timePairsTableBuilder.append(table)
        timePairsTableBuilder.append(tr)
        timePairsTableBuilder.append(th + getString(R.string.work_sched_test_1) + thC)
        timePairsTableBuilder.append(th + getString(R.string.time_pair_test2) + thC)
        timePairsTableBuilder.append(th + getString(R.string.t_atest3) + thC)
        timePairsTableBuilder.append(th + getString(R.string.t_btest3) + thC)
        timePairsTableBuilder.append(trC)
        cntWork = 0
        for (rp in 0..15)
            getRelVremPar(timePairsTableBuilder, rp, mPProgR)
        timePairsTableBuilder.append(tableC)
        return timePairsTableBuilder
    }

    private fun getRelVremPar(builder: StringBuilder, rp: Int, mPProgR: List<Opprog>) {
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
                builder.append(
                    td + String.format(
                        "%02d:%02d",
                        (mPProgR[rp].TPro[itemIndex].Ton) / 60,
                        (mPProgR[rp].TPro[itemIndex].Ton) % 60
                    )
                            + tdC
                )
                builder.append(
                    td + String.format(
                        "%02d:%02d",
                        (mPProgR[rp].TPro[itemIndex].Toff) / 60,
                        (mPProgR[rp].TPro[itemIndex].Toff) % 60
                    )
                            + tdC
                )
                builder.append(trC)
            }
        }
    }


    private fun generateTelegramSync(
        builder: java.lang.StringBuilder
    ) {
        builder.append(h2 + getString(R.string.sync_telegrams) + h2C)
        builder.append(table)
        getRasterHeadStringH(builder)
        getRasterHeadStringTop(builder)
        getRasterHeadStringBottom(builder)
        for (i in 0..4)
            getRasterStringSync(builder, mTelegSync[i].Cmd, i)

        builder.append(tableC)
    }

    private fun generateSyncTelegramDoW(
        builder: java.lang.StringBuilder
    ) {
        builder.append(h2 + getString(R.string.sync_telegrams_dow) + h2C)
        builder.append(table)
        getRasterHeadStringH(builder)
        getRasterHeadStringTop(builder)
        getRasterHeadStringBottom(builder)

        for (i in 0..7)
            getRasterString(builder, mTlgFnD[i].Cmd, i, 'a')

        builder.append(tableC)
    }

    private fun generateAdditionalTelegram(
        builder: java.lang.StringBuilder
    ) {
        builder.append(h2 + getString(R.string.additional_telegrams) + h2C)
        builder.append(table)
        getRasterHeadStringH(builder)
        getRasterHeadStringTop(builder)
        getRasterHeadStringBottom(builder)

        getRasterString(builder, mOp50Prij.tlg[0].tel1.Cmd, 1, 'a')
        getRasterString(builder, mOp50Prij.tlg[1].tel1.Cmd, 1, 'b')

        getRasterString(builder, mOp50Prij.tlg[2].tel1.Cmd, 1, 'a')
        getRasterString(builder, mOp50Prij.tlg[3].tel1.Cmd, 1, 'b')

        getRasterString(builder, mOp50Prij.tlg[4].tel1.Cmd, 1, 'a')
        getRasterString(builder, mOp50Prij.tlg[5].tel1.Cmd, 1, 'b')

        getRasterString(builder, mOp50Prij.tlg[6].tel1.Cmd, 1, 'a')
        getRasterString(builder, mOp50Prij.tlg[7].tel1.Cmd, 1, 'b')

        builder.append(tableC)
    }

    private fun generateClassicTelegram(
        builder: StringBuilder
    ) {
        builder.append(h2 + getString(R.string.classic_telegram) + h2C)
        builder.append(table)
        getRasterHeadStringH(builder)
        getRasterHeadStringTop(builder)
        getRasterHeadStringBottom(builder)

        getRasterString(builder, mOp50Prij.TlgRel1.Uk, 1, 'a')
        getRasterString(builder, mOp50Prij.TlgRel1.Isk, 1, 'b')

        getRasterString(builder, mOp50Prij.TlgRel2.Uk, 1, 'a')
        getRasterString(builder, mOp50Prij.TlgRel2.Isk, 1, 'b')

        getRasterString(builder, mOp50Prij.TlgRel3.Uk, 1, 'a')
        getRasterString(builder, mOp50Prij.TlgRel3.Isk, 1, 'b')

        getRasterString(builder, mOp50Prij.TlgRel4.Uk, 1, 'a')
        getRasterString(builder, mOp50Prij.TlgRel4.Isk, 1, 'b')

        builder.append(tableC)
    }


    private fun getRasterString(builder: StringBuilder, t: TelegCMD, num: Int, ch: Char) {
        builder.append(tr)
        builder.append(th + getString(R.string.unknown) + thC)
        builder.append(th + String.format("R%d %c", num, ch) + thC)
        for (iBimp in 0..49) {
            val nBitNumber = iBimp % 8
            val nByteNumber = iBimp / 8

            val N = t.NeutImp?.get(nByteNumber)!!.toInt() and (0x80 shr nBitNumber)
            val A = t.AktiImp?.get(nByteNumber)!!.toInt() and (0x80 shr nBitNumber)

            if (IsCZ44raster && iBimp == 44)
                break

            if (A != 0 && N != 0)
                builder.append(tdImpNeAkt + b + getString(R.string.plus) + bC + tdC)
            else if (A == 0 && N != 0)
                builder.append(tdImpAkt + b + getString(R.string.minus) + bC + tdC)
            else
                builder.append(tdImpNeutr + tdC)
        }
        builder.append(trC)
    }

    private fun getRasterStringSync(builder: StringBuilder, t: TelegCMD, x: Int) {
        builder.append(tr)
        builder.append(th + getString(R.string.unknown) + thC)
        builder.append(th + getSyncTime(mOp50Prij.SinhTime?.get(x)!!, m_HWVerPri) + thC)

        for (iBimp in 0..49) {
            val nBitNumber = iBimp % 8
            val nByteNumber = iBimp / 8

            val N = t.NeutImp?.get(nByteNumber)!!.toInt() and (0x80 shr nBitNumber)
            val A = t.AktiImp?.get(nByteNumber)!!.toInt() and (0x80 shr nBitNumber)
            if (IsCZ44raster && iBimp == 44)
                break

            if (A != 0 && N != 0)
                builder.append(tdImpNeAkt + b + getString(R.string.plus) + bC + tdC)
            else if (A == 0 && N != 0)
                builder.append(tdImpAkt + b + getString(R.string.minus) + bC + tdC)
            else
                builder.append(tdImpNeutr + tdC)
        }
        builder.append(trC)
    }

    private fun getSyncTime(t: Int, ver: Int): String? {
        val datstr: String
        val tstr: String
        val stime: Int
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
            datstr = String.format(
                "%02d:%02d:%02d",
                rtctime.b?.get(2)!!.toInt() and 0x1F,
                rtctime.b?.get(1)!!.toInt(),
                rtctime.b?.get(0)!!.toInt()
            )
            tmpi = rtctime.b!![2].toInt() shr 5
            if (tmpi == 0) {
                tmpi = 7
            } else {
                tmpi--
            }
        }
        tstr =
            String.format("%s>> %s", getStringArray(R.array.dan_sync_tg, tmpi % 8), datstr)
        return tstr
    }

    private fun getRasterHeadStringH(builder: StringBuilder) {
        builder.append(tr)

        builder.append(th + getString(R.string.name) + thC)
        builder.append(th + getString(R.string.telegram) + thC)
        builder.append(thcol4 + getString(R.string.a) + thC)
        builder.append(thcol8 + getString(R.string.b) + thC)
        for (i in 4..19)
            builder.append(thcol2 + String.format("DP%d", i - 3) + thC)
        builder.append(trC)
    }

    private fun getRasterHeadStringTop(builder: StringBuilder) {
        builder.append(tr)
        builder.append(th + thC)
        builder.append(th + thC)

        for (i in 1..4)
            builder.append(th + i + thC)


        for (i in 1..8)
            builder.append(th + i + thC)

        for (i in 14..45 step 2) {
            builder.append(th + getString(R.string.z) + thC)
            builder.append(th + getString(R.string.v) + thC)
        }

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
        builder.append(
            td +
                    String.format("MTK-%d-%s-V-%d", mTip + 1, version, mSoftwareVersionPri)
                    + tdC
        )
        builder.append(trC)

        if (fVis_RefPrij) {
            builder.append(tr)
            builder.append(th + getString(R.string.hdo_frequency) + thC)

            if (mSoftwareVersionPri >= 90) {
                if (mParFilteraCF.BROJ >= 0)
                    builder.append(
                        td + String.format(
                            "%4.2f Hz",
                            DataUtils.tbparfiltera98mhz()[mParFilteraCF.BROJ].fre
                        )
                                + tdC
                    )
            } else
                if (mParFiltera.BROJ >= 0)
                    if (mSoftwareVersionPri < 80)
                        builder.append(
                            td + String.format(
                                "%4.2f Hz",
                                DataUtils.tbParFiltera()[mParFiltera.BROJ].fre
                            ) + tdC
                        )
                    else
                        builder.append(
                            td + String.format(
                                "%4.2f Hz",
                                DataUtils.tbparfiltera98mhz()[mParFiltera.BROJ].fre
                            ) + tdC
                        )

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
                val dataStr =
                    if ((mOpPrij.ParFlags.toInt() and 0x1) != 0)
                        getString(R.string.yes)
                    else getString(R.string.no)
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
        mTelegSync: List<Telegram>
        ,
        mTlgFnd: List<Telegram>
    ) {
        if (mSoftwareVersionPri >= 96)
            getTlg50ParV96(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnd)
    }

    private fun getTlg50ParV96(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mTelegSync: List<Telegram>
        ,
        mTlgFnd: List<Telegram>
    ) {
        if (m_CFG.cID >= 0x8C) {
            getTlgID50ParV96(mgaddr, dbuf, mOp50Prij, mTelegSync, mTlgFnd)
        } else {
            when (mgaddr.objectt) {
                0 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel1)
                1 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel2)
                2 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel3)
                3 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel4)
            }
        }
    }

    private fun getTlgID50ParV96(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mTelegSync: List<Telegram>
        ,
        mTlgFnd: List<Telegram>
    ) {
        when (mgaddr.objectt) {
            0 -> storeDataTlgRel(dbuf, mOp50Prij.TlgRel1)
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
            8 -> {
                storeDataTlgFn(dbuf, mTelegSync[0])
                storeDataTlgFn(dbuf, mTelegSync[1])
            }
            9 -> {
                storeDataTlgFn(dbuf, mTelegSync[2])
                storeDataTlgFn(dbuf, mTelegSync[3])
                storeDataTlgFn(dbuf, mTelegSync[4])

            }
            10 -> {
                storeDataTlgFn(dbuf, mTlgFnd[0])
                storeDataTlgFn(dbuf, mTlgFnd[1])
                storeDataTlgFn(dbuf, mTlgFnd[2])
            }
            11 -> {
                storeDataTlgFn(dbuf, mTlgFnd[3])
                storeDataTlgFn(dbuf, mTlgFnd[4])
            }
            12 -> {
                storeDataTlgFn(dbuf, mTlgFnd[5])
                storeDataTlgFn(dbuf, mTlgFnd[6])
                storeDataTlgFn(dbuf, mTlgFnd[7])
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

        val temp: Int = dbuf[globalIndex++].toInt()
        var tempUp: Int = dbuf[globalIndex++].toInt()
        tempUp = tempUp shl 8
        fn.ID = temp or tempUp
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

        val temp: Int = dbuf[globalIndex++].toInt()
        var tempUp: Int = dbuf[globalIndex++].toInt()
        tempUp = tempUp shl 8

        tlgRel.ID = temp or tempUp
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
        mOpPrij.apply {
            VOpRe.VakProR1 = setOprelI(dbuf)
            VOpRe.VakProR2 = setOprelI(dbuf)
            VOpRe.VakProR3 = setOprelI(dbuf)
            VOpRe.VakProR4 = setOprelI(dbuf)

            VOpRe.StaPrij = dbuf[globalIndex++]

            if (mSoftwareVersionPri < 95) {
                mOpPrij.ParFlags = dbuf[globalIndex++]

                StaR1PwON_OFF = dbuf[globalIndex++]
                StaR2PwON_OFF = dbuf[globalIndex++]
                StaR3PwON_OFF = dbuf[globalIndex++]
                StaR4PwON_OFF = dbuf[globalIndex++]
            }
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

        if (inCik)
            mOp50Prij.WDaySinh = dbuf[globalIndex++]

        if (m_CFG.cID == 100) {
            globalIndex++
            globalIndex++
            globalIndex++
            globalIndex++
        } else if (m_CFG.cID == 130 || m_CFG.cID == 0x8C) {
            globalIndex++
            globalIndex++
        }
        val sinhTimes = mutableListOf<Int>()
        for (i in 0..4) {
            sinhTimes.add(setOprel4I(dbuf))
        }
        mOp50Prij.SinhTime = sinhTimes.toIntArray()

        if (!inCik)
            return

        val b1 = dbuf[globalIndex++]
        mOpPrij.VCRel1Tu = dbuf[globalIndex++]
        mOpPrij.VC1R1 = setOprelI(dbuf)

        val b2 = dbuf[globalIndex++]
        mOpPrij.VCRel2Tu = dbuf[globalIndex++]
        mOpPrij.VC1R2 = setOprelI(dbuf)

        val b3 = dbuf[globalIndex++]
        mOpPrij.VCRel3Tu = dbuf[globalIndex++]
        mOpPrij.VC1R3 = setOprelI(dbuf)

        val b4 = dbuf[globalIndex++]
        mOpPrij.VCRel4Tu = dbuf[globalIndex++]
        mOpPrij.VC1R4 = setOprelI(dbuf)

        mOpPrij.CRelXSw = byteArrayOf(b1, b2, b3, b4)


        if (m_CFG.cID == 120 || m_HWVerPri == Const.Data.TIP_PS)
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
            CLOGENFLGS = intArrayOf(setOprelI(dbuf), setOprelI(dbuf), setOprelI(dbuf))
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
            status2 = dbuf[globalIndex++]
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
            wiper.status = (0x80 + 0x20).toByte()
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

    private fun getTparPar(dbuf: ByteArray)
            : Opprog {
        val oPProg = Opprog()


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
            11


        x.b[1] = dbuf[globalIndex++]

        if (mSoftwareVersionPri >= 40)
            x.b[0] = dbuf[globalIndex++]

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


    private fun hasNextLine(data: ByteArray)
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
                    mline[i] = 0
                    m_Dateerr++
                    return false
                }
            }
            mline[i++] = data[m_cntxx++]
        }
        m_Dateerr++
        return false
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

    private fun HtoB(ch: Char)
            : Char {
        if (ch in '0'..'9') {
            isCheck = true
            return (ch - '0').toChar()
        }
        if (ch in 'A'..'F') {
            isCheck = true
            return (ch - 'A' + 0xA).toChar()
        }
        isCheck = false
        return ch
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