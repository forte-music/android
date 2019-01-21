package me.a0xcaff.forte.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.*
import me.a0xcaff.forte.default

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

class PlaybackServiceConnection(
    private val context: Context
) {
    private val _state = MutableLiveData<ConnectionState>().default(ConnectionState.Disconnected)
    val state: LiveData<ConnectionState> = _state

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
            throw IllegalStateException("Can't bind already bound or binding service.")
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

    companion object {
        fun withProcessObserver(context: Context) =
            PlaybackServiceConnection(context).also {
                attachProcessObserver(it)
            }
    }
}

fun attachProcessObserver(playbackServiceConnection: PlaybackServiceConnection) {
    ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onApplicationStart() =
            playbackServiceConnection.bind()

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onApplicationStop() =
            playbackServiceConnection.unbind()
    })
}
