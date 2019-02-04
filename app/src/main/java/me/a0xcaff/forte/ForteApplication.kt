package me.a0xcaff.forte

import android.app.Application
import me.a0xcaff.forte.di.Module
import me.a0xcaff.forte.di.PlaybackServiceModule
import org.koin.android.ext.android.startKoin

class ForteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startStetho(this)
        startKoin(this, listOf(Module, PlaybackServiceModule))
    }
}
