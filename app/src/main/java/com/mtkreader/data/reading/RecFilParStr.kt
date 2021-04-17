package com.mtkreader.data.reading

class RecFilParStr {

    companion object {
        const val PARID_SIZE = 8
        const val PARIDFILE_SIZE = 16
    }

    var CreateSite = ByteArray(PARID_SIZE)
    var IDCreate = ByteArray(PARID_SIZE)
    var IDFile = ByteArray(PARIDFILE_SIZE)
}