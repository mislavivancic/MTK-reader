package com.mtkreader.services

import com.mtkreader.contracts.DisplayDataContract

class DisplayServiceImpl : DisplayDataContract.DisplayService {

    override fun generateHtml(): String = "Test"
}