package me.a0xcaff.forte.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class PlaybackServiceConnection(
    private val context: Context,
    private val onBound: (PlaybackServiceBinder, Lifecycle) -> Unit
) : ServiceConnection {
    private var lifecycle: Lifecycle? = null
    private var bindingRequested = false

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val bindingLifecycle = Lifecycle()
        lifecycle = bindingLifecycle
        onBound(service as PlaybackServiceBinder, bindingLifecycle)
    }

    override fun onBindingDied(name: ComponentName?) {
        handleUnbind()
    }

    override fun onNullBinding(name: ComponentName?) {
        throw IllegalStateException("PlaybackServiceConnection got null binding")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        handleUnbind()
    }

    fun bind() {
        if (bindingRequested) {
            throw IllegalStateException("Tried to bind twice to PlaybackService")
        }

        val intent = Intent(context, PlaybackService::class.java)
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

    class Lifecycle {
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
