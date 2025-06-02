package com.foss.naksha.android

import android.app.Application
import com.foss.naksha.android.di.appModule
import com.foss.naksha.android.di.viewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule, viewModel)
        }
    }
}