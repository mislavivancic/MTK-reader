package com.mtkreader.data.reading

class m_LoopPar {

    var State:Byte=0
    var LastCmd:Byte=0    //0 nepoznata  1 ON 2 OFF
    var Tpar:WTONOFF=WTONOFF(0,0)


    //typedef struct {
    //    byte State;		// ne koristi se
    //    byte LastCmd;		//0 nepoznata  1 ON 2 OFF
    //    WTONOFF  Tpar;

    //} LOOPTIMSTR;

    //typedef struct{
    //    WORD ton;
    //    WORD toff;
    //}




}

data class WTONOFF(
    var ton:Short=0,
    var toff:Short=0
)