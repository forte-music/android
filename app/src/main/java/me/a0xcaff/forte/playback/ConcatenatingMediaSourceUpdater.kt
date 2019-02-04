package me.a0xcaff.forte.playback

import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener

/**
 * Keeps a [ConcatenatingMediaSource] up to date with values from an observable list.
 */
class ConcatenatingMediaSourceUpdater(
    private val concatenatingMediaSource: ConcatenatingMediaSource,
    private val mediaSourceFactory: ExtractorMediaSource.Factory
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
        mediaSourceFactory.createMediaSource(item.songUri).apply {
            addEventListener(Handler(Looper.getMainLooper()), PrefetchMediaSourceListener(item))
        }
}

class PrefetchMediaSourceListener(
    private val item: QueueItem
) : DefaultMediaSourceEventListener() {
    override fun onLoadStarted(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
        @Suppress("DeferredResultUnused")
        item.song
    }

    override fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        @Suppress("DeferredResultUnused")
        item.song
    }
}
