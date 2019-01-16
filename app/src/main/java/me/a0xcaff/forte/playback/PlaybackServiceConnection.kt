package me.a0xcaff.forte.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

sealed class ConnectionState {
    /**
     * Starts in this state and enters this state after [PlaybackServiceConnection.unbind] is called.
     */
    object Disconnected : ConnectionState()

    /**
     * Connecting to the service. Enters this state after [ServiceConnection.onServiceDisconnected]. Goes to [Connected]
     * or [Died].
     */
    object Connecting : ConnectionState()

    /**
     * The connection died and will never be revived. The connection goes into this state after
     * [ServiceConnection.onBindingDied] is called. This state is terminal.
     */
    object Died : ConnectionState()

    /**
     * The binder for this service is available.
     */
    class Connected(
        val binder: PlaybackServiceBinder,
        val onUnbind: EventReceiver<Unit>
    ) : ConnectionState()
}

class ObservableDataHolderImpl<TValue>(initialValue: TValue) : Event<TValue>(), ObservableDataHolder<TValue> {
    init {
        dispatch(initialValue)
    }

    override var value: TValue = initialValue
        set(value) {
            dispatch(value)
            field = value
        }

    override fun observe(handler: (TValue) -> Unit) {
        super.observe(handler)
        handler(value)
    }
}

interface ObservableDataHolder<TValue> : EventReceiver<TValue> {
    val value: TValue
}

class PlaybackServiceConnection(
    private val context: Context
) {
    private val _state = ObservableDataHolderImpl<ConnectionState>(ConnectionState.Disconnected)

    val state: ObservableDataHolder<ConnectionState>
        get() = _state

    private var connection: Connection? = null

    /**
     * Event dispatched before unbinding the service. Should release any references to the binder.
     */
    private val onUnbind = Event<Unit>()

    /**
     * Start connecting to the service. If already connecting, does nothing.
     */
    fun bind() {
        if (_state.value != ConnectionState.Disconnected) {
            return
        }

        val intent = Intent(context, PlaybackService::class.java)

        val newConnection = Connection()
        connection = newConnection

        context.bindService(intent, newConnection, Context.BIND_AUTO_CREATE)
        _state.value = ConnectionState.Connecting
    }

    /**
     * Disconnect from the service. If not bound, does nothing.
     */
    fun unbind() {
        if (_state.value == ConnectionState.Disconnected || _state.value == ConnectionState.Died) {
            // Nothing to do, already unbound.
            return
        }

        handleUnbind()
        _state.value = ConnectionState.Disconnected
    }

    private fun handleUnbind() {
        val currentConnection = connection!!
        onUnbind.dispatch(Unit)
        context.unbindService(currentConnection)
        connection = null
    }

    inner class Connection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            val playbackService = service as PlaybackServiceBinder
            _state.value = ConnectionState.Connected(playbackService, onUnbind)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _state.value = ConnectionState.Connecting
        }

        override fun onBindingDied(name: ComponentName?) {
            handleUnbind()
            _state.value = ConnectionState.Died
        }

        override fun onNullBinding(name: ComponentName?) {
            handleUnbind()
            throw IllegalStateException("Unexpected null binding")
        }
    }
}
