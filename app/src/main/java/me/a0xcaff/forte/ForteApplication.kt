package me.a0xcaff.forte

import android.app.Application
import me.a0xcaff.forte.di.Module
import org.koin.android.ext.android.startKoin

class ForteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(Module))
    }
}
