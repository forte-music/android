package me.a0xcaff.forte

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class MediaPlaybackServiceConnection(
    private val context: Context,
    private val onBound: (Lifecycle) -> Unit
) : ServiceConnection {
    private var lifecycle: Lifecycle? = null
    private var bindingRequested = false

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val bindingLifecycle = Lifecycle(service as MediaPlaybackService.Binder)
        lifecycle = bindingLifecycle
        onBound(bindingLifecycle)
    }

    override fun onBindingDied(name: ComponentName?) {
        handleUnbind()
    }

    override fun onNullBinding(name: ComponentName?) {
        throw IllegalStateException("MediaPlaybackServiceConnection got null binding")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        handleUnbind()
    }

    fun bind() {
        if (bindingRequested) {
            throw IllegalStateException("Tried to bind twice to MediaPlaybackService")
        }

        val intent = Intent(context, MediaPlaybackService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        bindingRequested = true
    }

    fun tryUnbind() {
        if (bindingRequested) {
            context.unbindService(this)
            bindingRequested = false
        }

        handleUnbind()
    }

    private fun handleUnbind() {
        lifecycle?.handleUnbind()
        lifecycle = null
    }

    inner class Lifecycle(val service: MediaPlaybackService.Binder) {
        private val unbindListeners = mutableListOf<() -> Unit>()

        fun registerOnUnbind(listener: () -> Unit) {
            unbindListeners.add(listener)
        }

        fun handleUnbind() {
            while (unbindListeners.size > 0) {
                val top = unbindListeners.removeAt(unbindListeners.lastIndex)
                top()
            }
        }
    }
}
