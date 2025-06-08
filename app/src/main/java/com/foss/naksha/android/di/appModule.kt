package com.foss.naksha.android.di

import com.foss.naksha.android.data_access.ipc.IPCProvider
import com.foss.naksha.android.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import to.holepunch.bare.android.manager.LocationManager

val appModule = module {
    single { IPCProvider.ipc }

    single { LocationManager(get()) }
}

val viewModel = module { viewModel { HomeViewModel(get(), get()) } }

