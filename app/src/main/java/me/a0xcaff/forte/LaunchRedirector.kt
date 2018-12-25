package me.a0xcaff.forte

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

internal class LaunchRedirector(
    private val config: Config,
    private val onConnect: () -> Unit
) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (config.serverUrl == null) {
            onConnect()
        }
    }
}
