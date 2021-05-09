package com.mtkreader.data.reading

class TelegCMD {
    var AktiImp: ByteArray = ByteArray(7)
    var BrAkImp: Byte = 0
    var NeutImp: ByteArray = ByteArray(7)
    var Fn: Byte = 0
    var CmdDB=ByteArray(15){0}
        get()=AktiImp+BrAkImp+NeutImp
}