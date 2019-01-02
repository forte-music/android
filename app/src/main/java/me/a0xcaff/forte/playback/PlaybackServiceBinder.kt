package me.a0xcaff.forte.playback

import android.os.Binder
import com.google.android.exoplayer2.C
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
    /**
     * Current playback state.
     */
    val state: PlaybackState

    /**
     * Whether playback will continue when [state] becomes [PlaybackState.Ready].
     *
     * See [Player.setPlayWhenReady] and [Player.getPlayWhenReady]
     */
    var playWhenReady: Boolean

    /**
     * Emits an event whenever [state] or [playWhenReady] changes.
     */
    val playbackStateChanged: EventReceiver<Unit>

    /**
     * The items currently in the queue. The queue may include already played items.
     */
    val queue: Queue

    /**
     * The current position in the queue. Can be [C.INDEX_UNSET].
     */
    val queuePosition: Int

    /**
     * The item currently playing. Null if there is none.
     */
    val nowPlaying: QueueItem?
}

/**
 * Interface of [PlaybackService] exposed to the rest of the application.
 */
class PlaybackServiceBinderImpl(
    private val player: Player,
    override val queue: Queue
) : Binder(), PlaybackServiceBinder {
    override val state: PlaybackState
        get() = PlaybackState.fromExoPlayer(player.playbackState)

    override var playWhenReady: Boolean
        get() = player.playWhenReady
        set(value) {
            player.playWhenReady = value
        }

    private val _playbackStateChanged = Event<Unit>()
    override val playbackStateChanged: EventReceiver<Unit>
        get() = _playbackStateChanged

    private val listener = Listener()

    override val queuePosition: Int
        get() = player.currentWindowIndex

    override val nowPlaying: QueueItem?
        get() = queue.getNowPlaying(player)

    init {
        player.addListener(listener)
    }

    fun release() {
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
