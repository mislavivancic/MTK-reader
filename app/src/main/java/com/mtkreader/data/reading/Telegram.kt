package com.mtkreader.data.reading

class Telegram {
    var Cmd: TelegCMD = TelegCMD()

    var CmdDB=ByteArray(15){0}
        get()=Cmd.AktiImp+Cmd.BrAkImp+Cmd.NeutImp //TODO join arr

}