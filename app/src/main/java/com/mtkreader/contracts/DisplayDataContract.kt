package com.mtkreader.contracts

import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment
import com.mtkreader.data.DataStructures

interface DisplayDataContract {

    interface View : ErrorHandlingFragment {
        fun displayData(dataString: String)
    }

    interface Presenter : AutoDisposePresenter {
        fun processData(header: ByteArray, data: ByteArray)
    }

    interface DisplayService {
        fun generateHtml(dataStructures: DataStructures): String
    }

    interface ProcessService {
        fun processData(header: ByteArray, data: ByteArray): DataStructures
    }
}
