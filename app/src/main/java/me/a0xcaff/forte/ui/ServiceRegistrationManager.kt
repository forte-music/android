package me.a0xcaff.forte.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.EventReceiver
import me.a0xcaff.forte.playback.PlaybackServiceBinder

class ServiceRegistrationManager(
    val onBound: (bound: PlaybackServiceBinder) -> Unit,
    val onUnbound: (unbinding: PlaybackServiceBinder) -> Unit
) {
    var binder: PlaybackServiceBinder? = null
        private set

    val isBound: Boolean
        get() = binder != null

    // TODO: Remove Usages
    fun mustBeBound(): PlaybackServiceBinder =
        binder
            ?: throw java.lang.IllegalStateException("ServiceRegistrationManager mustBeBound called on unbound binder")

    private fun registerService(service: PlaybackServiceBinder, onUnbind: EventReceiver<Unit>) {
        binder = service
        onBound(service)
        onUnbind.observe { unregister() }
    }

    fun register(liveData: LiveData<ConnectionState>, lifecycleOwner: LifecycleOwner) {
        liveData.observe(lifecycleOwner, Observer { connectionState ->
            when (connectionState) {
                is ConnectionState.Connected ->
                    registerService(connectionState.binder, connectionState.onUnbind)
            }
        })
    }

    private fun unregister() {
        val activeBinder = mustBeBound()
        onUnbound(activeBinder)
        binder = null
    }
}