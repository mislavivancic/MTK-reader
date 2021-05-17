package com.mtkreader

import android.app.Application
import android.content.Context
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.contracts.TimeContract
import com.mtkreader.services.*
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MtkApplication : Application() {

    private val mtkModule = module {

        // services
        single<MonitorContract.Service> { MonitorServiceImpl() }
        single<DisplayDataContract.DisplayService> { DisplayServiceImpl() }
        single<DisplayDataContract.ProcessService> { ProcessDataServiceImpl() }
        single<TimeContract.Service> { TimeServiceImpl() }
        single<ParamsWriteContract.FillDataStructuresService> { ParamsWriteFillDataStructuresService() }
        single<ParamsWriteContract.WriteDataService> { WriteDataService() }

        // utils
        single { RxBluetooth(this@MtkApplication) }

        single<Context> { this@MtkApplication }


    }

    override fun onCreate() {
        super.onCreate()

        RxJavaPlugins.setErrorHandler { throwable: Throwable -> (throwable as? UndeliverableException)?.printStackTrace() }

        startKoin {
            modules(mtkModule)
        }
    }
}
