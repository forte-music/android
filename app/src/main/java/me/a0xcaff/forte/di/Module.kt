package me.a0xcaff.forte.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import me.a0xcaff.forte.Config
import me.a0xcaff.forte.ConfigImpl
import me.a0xcaff.forte.ServerValidator
import me.a0xcaff.forte.ServerValidatorImpl
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.ui.connect.ConnectActivityViewModel
import me.a0xcaff.forte.ui.view.BottomSheetViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val Module = module {
    single {
        OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }
    single {
        Picasso.Builder(androidContext())
            .downloader(OkHttp3Downloader(get<OkHttpClient>()))
            .loggingEnabled(true)
            .build()
    }
    single { ServerValidatorImpl(get()) as ServerValidator }
    viewModel { ConnectActivityViewModel(serverValidator = get(), config = get()) }
    viewModel { BottomSheetViewModel() }
    single { ConfigImpl.from(androidContext()) as Config }
    single { PlaybackServiceConnection.withProcessObserver(androidContext()) }
    factory { OkHttpDataSourceFactory(get<OkHttpClient>(), "Forte Music Android") }
}
