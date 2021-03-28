package com.mtkreader.contracts

import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface TimeContract {

    interface View : ErrorHandlingFragment {
        fun displayData(dataString: String)
    }

    interface Presenter : AutoDisposePresenter {
        fun processData(data: ByteArray)
    }

    interface ProcessService {
        fun processData(data: ByteArray): String
    }
}