package me.a0xcaff.forte.playback

import android.os.Binder
import com.google.android.exoplayer2.Player

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Ready : PlaybackState()
    object Ended : PlaybackState()

    companion object {
        fun fromExoPlayer(state: Int): PlaybackState = when (state) {
            Player.STATE_IDLE -> PlaybackState.Idle
            Player.STATE_BUFFERING -> PlaybackState.Buffering
            Player.STATE_READY -> PlaybackState.Ready
            Player.STATE_ENDED -> PlaybackState.Ended
            else -> throw IllegalArgumentException("fromExoPlayer doesn't understand state id: $state")
        }
    }
}

interface PlaybackServiceBinder {
    val state: PlaybackState
    val playWhenReady: Boolean
    val playbackStateChanged: EventReceiver<Unit>

    fun play()
    fun pause()
    fun release()
}

/**
 * Interface of [PlaybackService] exposed to the rest of the application.
 */
class PlaybackServiceBinderImpl(val player: Player) : Binder(), PlaybackServiceBinder {
    override val state: PlaybackState
        get() = PlaybackState.fromExoPlayer(player.playbackState)

    override val playWhenReady: Boolean
        get() = player.playWhenReady

    private val _playbackStateChanged = Event<Unit>()
    override val playbackStateChanged: EventReceiver<Unit>
        get() = _playbackStateChanged

    private val listener = Listener()

    init {
        player.addListener(listener)
    }

    override fun play() {
        player.playWhenReady = true
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun release() {
        player.removeListener(listener)
    }

    inner class Listener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            _playbackStateChanged.dispatch(Unit)
        }
    }
}

interface EventReceiver<T> {
    fun observe(handler: (T) -> Unit)
    fun unObserve(handle: (T) -> Unit)
}

class Event<T> : EventReceiver<T> {
    private val handlers = arrayListOf<((T) -> Unit)>()
    override fun observe(handler: (T) -> Unit) {
        handlers.add(handler)
    }

    override fun unObserve(handle: (T) -> Unit) {
        handlers.remove(handle)
    }

    fun dispatch(value: T) {
        handlers.forEach { it(value) }
    }
}
