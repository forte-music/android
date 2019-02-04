package me.a0xcaff.forte.playback

import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import java.io.IOException

/**
 * A [MediaSourceEventListener] with default nop implementations for all methods.
 */
open class DefaultMediaSourceEventListener : MediaSourceEventListener {
    override fun onLoadStarted(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onDownstreamFormatChanged(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onUpstreamDiscarded(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
    }

    override fun onLoadCanceled(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onMediaPeriodReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
    }

    override fun onReadingStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
    }

    override fun onLoadCompleted(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onLoadError(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?,
        error: IOException?,
        wasCanceled: Boolean
    ) {
    }
}