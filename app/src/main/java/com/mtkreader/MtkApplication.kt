package com.mtkreader

import android.app.Application
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MtkApplication : Application() {

    private val mtkModule = module {

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