package com.mtkreader.contracts

import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface DisplayDataContract {

    interface View : ErrorHandlingFragment {
        fun displayData(dataString: String)
    }

    interface Presenter : AutoDisposePresenter {
        fun processData(data: CharArray)
    }

    interface DisplayService {
        fun generateHtml(): String
    }

    interface ProcessService {
        fun processData(data: CharArray)
    }
}