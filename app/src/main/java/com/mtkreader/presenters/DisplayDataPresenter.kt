package com.mtkreader.presenters

import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.DisplayDataContract

class DisplayDataPresenter(private val view: DisplayDataContract.View) : BasePresenter(view),
    DisplayDataContract.Presenter {


}