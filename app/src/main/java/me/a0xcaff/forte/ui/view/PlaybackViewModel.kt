package me.a0xcaff.forte.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.NowPlayingInfo
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.observeNowAndUntil
import me.a0xcaff.forte.switchMap
import me.a0xcaff.forte.ui.formatTime

class PlaybackViewModel(val connection: PlaybackServiceConnection) : ViewModel() {
    val playWhenReady: LiveData<Boolean> = connection.state.switchMap { connectionState ->
        if (connectionState !is ConnectionState.Connected) return@switchMap null

        MutableLiveData<Boolean>().apply {
            connectionState.binder.playbackStateChanged.observeNowAndUntil(connectionState.onUnbind) {
                value = connectionState.binder.playWhenReady
            }
        }
    }

    val canSkipForwards: LiveData<Boolean> = connection.state.switchMap { connectionState ->
        if (connectionState !is ConnectionState.Connected) return@switchMap null

        MutableLiveData<Boolean>().apply {
            connectionState.binder.queuePositionChanged.observeNowAndUntil(connectionState.onUnbind) {
                value = connectionState.binder.hasNext
            }
        }
    }

    val nowPlaying: LiveData<NowPlayingInfo> = connection.state.switchMap { connectionState ->
        if (connectionState !is ConnectionState.Connected) return@switchMap null

        MutableLiveData<NowPlayingInfo>().apply {
            connectionState.binder.queuePositionChanged.observeNowAndUntil(connectionState.onUnbind) {
                value = connectionState.binder.nowPlaying!!
            }
        }
    }

    val duration: LiveData<String> = connection.state.switchMap { connectionState ->
        if (connectionState !is ConnectionState.Connected) return@switchMap null

        MutableLiveData<String>().apply {
            connectionState.binder.queuePositionChanged.observeNowAndUntil(connectionState.onUnbind) {
                val duration = connectionState.binder.nowPlaying?.duration ?: -1
                value = when (duration) {
                    -1L -> "--:--"
                    else -> formatTime(duration)
                }
            }
        }
    }

    fun togglePlayWhenReady() {
        val currentState = connection.state.value!! as? ConnectionState.Connected ?: return
        currentState.binder.playWhenReady = !playWhenReady.value!!
    }

    fun skipForwards() {
        val currentState = connection.state.value!! as? ConnectionState.Connected ?: return
        currentState.binder.next()
    }

    fun skipBackwards() {
        val currentState = connection.state.value!! as? ConnectionState.Connected ?: return
        currentState.binder.previous()
    }
}
