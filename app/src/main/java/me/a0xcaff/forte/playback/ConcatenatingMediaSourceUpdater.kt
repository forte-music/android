package me.a0xcaff.forte.playback

import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource

/**
 * Keeps a [ConcatenatingMediaSource] up to date with values from an observable list.
 */
class ConcatenatingMediaSourceUpdater(
    private val concatenatingMediaSource: ConcatenatingMediaSource,
    private val mediaSourceFactory: ExtractorMediaSource.Factory,
    private val quality: Quality,
    private val base: Uri
) : ObservableList.Observer<QueueItem> {
    // TODO: Updatable Quality

    override fun onItemMoved(fromPosition: Int, toPosition: Int) =
        concatenatingMediaSource.moveMediaSource(fromPosition, toPosition)

    override fun onItemRemoved(idx: Int) = concatenatingMediaSource.removeMediaSource(idx)

    override fun onItemRangeInserted(positionStart: Int, items: List<QueueItem>) {
        val mediaSources = items.map(this::makeMediaSource)
        concatenatingMediaSource.addMediaSources(positionStart, mediaSources)
    }

    override fun onItemRangeChanged(positionStart: Int, items: List<QueueItem>) {
        concatenatingMediaSource.removeMediaSourceRange(positionStart, positionStart + items.size)
        onItemRangeInserted(positionStart, items)
    }

    private fun makeMediaSource(item: QueueItem): MediaSource =
        mediaSourceFactory.createMediaSource(item.audioUri(base, quality))
}

