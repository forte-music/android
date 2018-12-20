package me.a0xcaff.forte

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent

internal class LaunchRedirector(
    private val config: Config,
    private val onConnect: () -> Unit
) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (config.getServerUrl() == null) {
            onConnect()
        }
    }
}
