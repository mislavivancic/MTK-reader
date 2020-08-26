package com.mtkreader.contracts

import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface DisplayDataContract {

    interface View : ErrorHandlingFragment {
    }

    interface Presenter : AutoDisposePresenter {
    }
}