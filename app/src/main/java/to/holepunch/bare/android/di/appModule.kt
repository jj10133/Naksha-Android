package to.holepunch.bare.android.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import to.holepunch.bare.android.data_access.ipc.IPCProvider
import to.holepunch.bare.android.viewmodel.HomeViewModel


val appModule = module {
    single {
        IPCProvider.ipc
    }
}

val viewModel = module {
    viewModel {
        HomeViewModel(get(), get())
    }
}