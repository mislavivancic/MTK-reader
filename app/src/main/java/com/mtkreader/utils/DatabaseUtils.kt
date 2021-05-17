package com.mtkreader.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.mtkreader.commons.Const
import com.mtkreader.hasFlag
import com.mtkreader.utils.DataUtils.byteArrayToHexString
import com.mtkreader.utils.DataUtils.hexStringToByteArray
import com.mtkreader.utils.DataUtils.hexToAscii
import net.alexandroid.utils.mylogkt.logI
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

class TelegramModel {
    //CREATE TABLE telegrams (
    //uid    INTEGER PRIMARY KEY
    //UNIQUE,
    //name   STRING  UNIQUE ON CONFLICT ROLLBACK,
    //descr  STRING,
    //kat    STRING,
    //tlg    BLOB    COLLATE BINARY,
    //tlg2   BLOB    COLLATE BINARY,
    //tid    INTEGER CHECK (tid < 65536)
    //UNIQUE,
    //raster INTEGER,
    //descr2 STRING
    //);

    var uid :Int=0
    var name   :String=""
    var descr  :String=""
    var kat    :String=""

    var tid :Int=0
    var raster :Int=0
    var descr2 :String=""

    var tlg    =ByteArray(0)
    var tlg2   =ByteArray(0)


    constructor(name: String,descr: String,kat: String,raster:Int,descr2:String) {
        this.name = name
        this.descr = descr
        this.kat = kat

        this.raster = raster
        this.descr2 = descr2
        this.kat = kat
    }
    constructor(name: String,descr: String,kat: String) {
        this.name = name
        this.descr = descr
        this.kat = kat
    }
    constructor(    uid    :Int
                    ,name   :String
                    ,descr  :String
                    ,kat    :String
                    ,tlg    :ByteArray
                    ,tlg2   :ByteArray
                    ,tid    :Int
                    ,raster :Int
                    ,descr2 :String
    ) {
        this.uid      =uid
        this.name     =name
        this.descr    =descr
        this.kat      =kat
        this.tlg      =tlg
        this.tlg2     =tlg2
        this.tid      =tid
        this.raster   =raster
        this.descr2   =descr2
    }
    override fun toString(): String {
        return "${name}-${descr}-${kat}"
    }
}

class AssetDatabaseOpenHelper(private val context: Context) {

    companion object {

        private val DB_NAME = "mtktlg.db"
    }

    fun openDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_NAME)


        if (!dbFile.exists()) {
            try {
                val checkDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE,null)

                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }

        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @SuppressLint("WrongConstant")
    private fun copyDatabase(dbFile: File) {
        val `is` = context.assets.open(DB_NAME)
        val os = FileOutputStream(dbFile)

        val buffer = ByteArray(1024)
        while (`is`.read(buffer) > 0) {
            os.write(buffer)
            Log.d("#DB", "writing>>")
        }

        os.flush()
        os.close()
        `is`.close()
        Log.d("#DB", "completed..")
    }
}






class DBQ: KoinComponent {
    constructor()
    val context: Context by inject()

    fun GetAllTelegrams(): ArrayList<TelegramModel> { //readAllTelegrams
        val telegrams = ArrayList<TelegramModel>()
        // = writableDatabase

        val adb = AssetDatabaseOpenHelper(context)

        val db =adb.openDatabase()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + "telegrams", null)
        } catch (e: SQLiteException) {
            //db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                var uid = cursor.getInt(cursor.getColumnIndex("uid"))
                var name = cursor.getString(cursor.getColumnIndex("name"))
                var descr = cursor.getString(cursor.getColumnIndex("descr"))
                var tlg1 = cursor.getBlob(cursor.getColumnIndex("tlg"))
                var tlg2 = cursor.getBlob(cursor.getColumnIndex("tlg"))
                var kat = cursor.getString(cursor.getColumnIndex("kat"))
                var descr2 = cursor.getString(cursor.getColumnIndex("descr2"))
                var raster = cursor.getInt(cursor.getColumnIndex("raster"))

                telegrams.add(TelegramModel(uid, name, descr, kat, tlg1, tlg2, 0, raster, descr2))
                cursor.moveToNext()
            }
        }
        return telegrams
    }



    fun GetTlgNameByContentK(tlg1:ByteArray,tlg2:ByteArray,raster:Int):String?
    {
        var res="IDSI_CMDUNKNW"
        if(tlg1.all { it.toInt()==0.toInt() } or
            tlg2.all { it.toInt()==0.toInt() }) res ="IDSI_CMDEMPTY"

        val query="select name from telegrams where tlg = x'${DataUtils.byteArrayToHexString(tlg1)}' and tlg2 = x'${DataUtils.byteArrayToHexString(tlg2)}' and raster = ${raster}"
        val telegrams = ArrayList<TelegramModel>()
        // = writableDatabase

        val adb = AssetDatabaseOpenHelper(context)

        val db =adb.openDatabase()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(query, null)
        } catch (e: SQLiteException) {
            //db.execSQL(SQL_CREATE_ENTRIES)
            return null
        }

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                res = cursor.getString(cursor.getColumnIndex("name"))
                cursor.moveToNext()
            }
        }
        return res




    }


    fun GetTlgNameByContent(tlg:ByteArray,raster:Int):String?
    {
        var res="IDSI_CMDUNKNW"
        if(tlg.all { it.toInt()==0.toInt() }) res ="IDSI_CMDEMPTY"

        val query="select name from telegrams where tlg = x'${DataUtils.byteArrayToHexString(tlg)}' and raster = ${raster}"
        val telegrams = ArrayList<TelegramModel>()
        // = writableDatabase

        val adb = AssetDatabaseOpenHelper(context)

        val db =adb.openDatabase()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(query, null)
        } catch (e: SQLiteException) {
            //db.execSQL(SQL_CREATE_ENTRIES)
            return null
        }

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                res = cursor.getString(cursor.getColumnIndex("name"))
                cursor.moveToNext()
            }
        }
        return res


    }

    fun GetTlgNamesMatchingRectlg(rectlg:ByteArray):List<String>{
        var res = mutableListOf<String>()

        var T=GetAllTelegrams()
        for (t in T) {
            if( (tlgmatches(t.tlg,rectlg) or tlgmatches(t.tlg2,rectlg) ) and (t.kat.compareTo("K")!=0)) {
                res.add(t.name)
                var imps = TelgImp2String(t.tlg, 4)
                Log.d("#DB", t.name + " - " + imps) //will show you database entries in Logcat.
            }
        }
        return res
    }


    fun tlgmatches(tlgdb: ByteArray, tlg: ByteArray): Boolean {

        if (tlgdb.all { it.toInt() == 0.toInt() }) return false //ako je telegram iz baze prazan
        if (tlg.all { it.toInt() == 0.toInt() }) return false//ako je primljeni telegram prazan
        for (i in 0..6) {
            for (j in 0..6) {
                val recimp = 8 * i + j + 1
                val dbimp = GetImpTLgAplus(tlgdb, recimp)
                val rescmp = tlg[i].toInt().hasFlag(Const.Data.bVtmask[j].toInt())

                if (rescmp)//ako 1 primljena//a u bazi mora bit/je 0
                    if (dbimp == "0")
                        return false
                if (!rescmp)// ako 0 primljena//a u bazi mora bit/je 1
                    if (dbimp == "1")
                        return false
                if (recimp > 50)
                    return true
            }
        }
        return false


    }




    private fun importDB() {
        val dir = Environment.getExternalStorageDirectory().absolutePath
        val sd = File(dir+"/mtktst")
        val data = Environment.getDataDirectory()
        var source: FileChannel? = null
        var destination: FileChannel? = null
        val backupDBPath = "/data/com.mtkreader/databases/mtktlg.db"
        val currentDBPath = "mtktlg.db"
        val currentDB = File(sd, currentDBPath)
        val backupDB = File(data, backupDBPath)
        try {
            source = FileInputStream(currentDB).channel
            destination = FileOutputStream(backupDB).channel
            destination.transferFrom(source, 0, source.size())
            source.close()
            destination.close()
            Toast.makeText(context, "Your Database is Imported !!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun TelgImp2String(teleg: ByteArray, m_BrojRast: Int):String {
        var arA = mutableListOf<String>()
        var arB = mutableListOf<String>()
        var arC = mutableListOf<String>()
        var arDP = mutableListOf<String>()

        var IsCZ44raster = (m_BrojRast == 4) or (m_BrojRast == 5)
        var maximp = if (IsCZ44raster) 44 else 50

        for (i in 0 until maximp) {
            var imp = GetImpTLgAplus(teleg, i + 1)
            when (i) {
                in 0..4 -> arA.add(imp!!)
                in 5..12 -> arB.add(imp!!)
                in 13..44 -> arDP.add(imp!!)
                else -> arC.add(imp!!)
            }
        }
        var str = "A[${arA.joinToString("")}]" +
                "B[${arB.joinToString("")}]" +
                "DP[${arDP.joinToString("")}]" +
                "C[${arC.joinToString("")}]"
        return str
    }

    fun GetImpTLgAplus(teleg:ByteArray, Imp:Int):String?
    {
        if (Imp == 0) return null;
        var ibimp = Imp - 1;

        var nBitNumber=ibimp % 8
        var nByteNumber= ibimp / 8
        var bitMask=(0x80 shr nBitNumber)

        var A = teleg[0+nByteNumber].toInt().hasFlag(bitMask)
        var N = teleg[8+nByteNumber].toInt().hasFlag(bitMask)

        if (N and A) return "1" //+3
        if ((N )and  (!A))return "0" //-2
        if ((!N) and (!A))return "." //neutralni1
        return null
    }



}



