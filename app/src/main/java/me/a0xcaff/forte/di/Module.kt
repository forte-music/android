package me.a0xcaff.forte.di

import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import me.a0xcaff.forte.Config
import me.a0xcaff.forte.ConfigImpl
import me.a0xcaff.forte.ServerValidator
import me.a0xcaff.forte.ServerValidatorImpl
import me.a0xcaff.forte.ui.connect.ConnectActivityViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val Module = module {
    single { OkHttpClient() }
    single { ServerValidatorImpl(get()) as ServerValidator }
    viewModel { ConnectActivityViewModel(serverValidator = get(), config = get()) }
    single { ConfigImpl.from(androidContext()) as Config }
    factory { OkHttpDataSourceFactory(get<OkHttpClient>(), "Forte Music Android") }
}
