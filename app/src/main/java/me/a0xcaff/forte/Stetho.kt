package me.a0xcaff.forte

import com.facebook.stetho.Stetho

fun startStetho(application: ForteApplication) {
    Stetho.initializeWithDefaults(application)
}
