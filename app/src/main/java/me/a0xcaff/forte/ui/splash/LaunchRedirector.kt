package me.a0xcaff.forte.ui.splash

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import me.a0xcaff.forte.Config

internal class LaunchRedirector(
    private val config: Config,
    private val onConnect: () -> Unit,
    private val onConnected: () -> Unit
) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (config.serverUrl == null) {
            onConnect()
        } else {
            onConnected()
        }
    }
}
