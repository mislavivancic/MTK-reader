package com.mtkreader

import android.app.Application
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.services.DisplayServiceImpl
import com.mtkreader.services.ProcessDataServiceImpl
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MtkApplication : Application() {

    private val mtkModule = module {

        // services
        single<DisplayDataContract.DisplayService> { DisplayServiceImpl() }
        single<DisplayDataContract.ProcessService> { ProcessDataServiceImpl() }

        // utils
        single { RxBluetooth(this@MtkApplication) }


    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(mtkModule)
        }
    }
}