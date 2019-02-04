package me.a0xcaff.forte.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import me.a0xcaff.forte.graphql.SongQueueQuery
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.observeNowAndUntil
import me.a0xcaff.forte.playback.returnNowOrLater
import me.a0xcaff.forte.switchMap
import me.a0xcaff.forte.ui.formatTime

class PlaybackViewModel(val connection: PlaybackServiceConnection) : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Main)

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

    val nowPlaying: LiveData<SongQueueQuery.Song> = connection.state.switchMap { connectionState ->
        if (connectionState !is ConnectionState.Connected) return@switchMap null

        MutableLiveData<SongQueueQuery.Song>().apply {
            connectionState.binder.queuePositionChanged.observeNowAndUntil(connectionState.onUnbind) {
                value = returnNowOrLater(scope, connectionState.binder.nowPlaying!!.item.song) {
                    value = it
                }
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

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
