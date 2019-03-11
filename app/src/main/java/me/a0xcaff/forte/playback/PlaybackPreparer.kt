package me.a0xcaff.forte.playback

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource

/**
 * Calls [SimpleExoPlayer.prepare] when [queue] goes from empty to non-empty.
 */
class PlaybackPreparer(
    private val queue: Queue,
    private val player: SimpleExoPlayer,
    private val mediaSource: ConcatenatingMediaSource
) : ObservableList.Observer<QueueItem> {
    override fun onItemRangeInserted(positionStart: Int, items: List<QueueItem>) {
        if (queue.items.size == items.size) {
            player.prepare(mediaSource)
        }
    }
}
