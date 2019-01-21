package me.a0xcaff.forte.playback

import android.os.Binder
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import me.a0xcaff.forte.previousOrBeginning

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

// TODO: Playing From Info
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
     * The item currently playing. Null if there is none.
     */
    val nowPlaying: NowPlayingInfo?

    val queuePositionChanged: EventReceiver<Unit>

    val hasNext: Boolean

    fun next()

    fun previous()
}

interface NowPlayingInfo {
    /**
     * Data used to play item in the queue.
     */
    val item: QueueItem

    /**
     * The current progress of the track as number of milliseconds played. Poll this value for progress updates.
     */
    val currentPosition: Long

    /**
     * The duration of track in milliseconds.
     */
    val duration: Long

    /**
     * Milliseconds buffered.
     */
    val bufferedPosition: Long
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
    override val playbackStateChanged: EventReceiver<Unit> = _playbackStateChanged

    private val listener = Listener()

    override val nowPlaying: NowPlayingInfo?
        get() = NowPlayingInfoImpl()

    private val _queuePositionChanged = Event<Unit>()
    override val queuePositionChanged: EventReceiver<Unit> = _queuePositionChanged

    override val hasNext: Boolean
        get() = player.hasNext()

    override fun next() = player.next()

    override fun previous() = player.previousOrBeginning()

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

        override fun onPositionDiscontinuity(reason: Int) {
            _queuePositionChanged.dispatch(Unit)
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            _queuePositionChanged.dispatch(Unit)
        }
    }

    inner class NowPlayingInfoImpl : NowPlayingInfo {
        override val item: QueueItem
            get() = queue.getNowPlaying(player)!!

        override val currentPosition: Long
            get() = player.currentPosition

        override val duration: Long
            get() = player.duration

        override val bufferedPosition: Long
            get() = player.bufferedPosition
    }
}

interface EventReceiver<T> {
    fun observe(handler: (T) -> Unit)
    fun unObserve(handle: (T) -> Unit)
}

fun <T> EventReceiver<T>.observeUntil(onUnbind: EventReceiver<Unit>, handler: (T) -> Unit) {
    observe(handler)
    onUnbind.observe { unObserve(handler) }
}

fun EventReceiver<Unit>.observeNowAndUntil(onUnbind: EventReceiver<Unit>, handler: (Unit) -> Unit) {
    handler(Unit)
    observeUntil(onUnbind, handler)
}

open class Event<T> : EventReceiver<T> {
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

    fun unObserveAll() {
        handlers.removeAll { true }
    }
}
