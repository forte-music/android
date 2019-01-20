package me.a0xcaff.forte.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.observeUntil
import me.a0xcaff.forte.switchMap

class PlaybackViewModel(val connection: PlaybackServiceConnection) : ViewModel() {
    val playWhenReady: LiveData<Boolean> = connection.state.switchMap { connectionState ->
        when (connectionState) {
            is ConnectionState.Connected -> MutableLiveData<Boolean>().apply {
                value = connectionState.binder.playWhenReady

                connectionState.binder.playbackStateChanged.observeUntil(connectionState.onUnbind) {
                    value = connectionState.binder.playWhenReady
                }
            }
            else -> null
        }
    }

    fun togglePlayWhenReady() {
        val currentState = connection.state.value!! as? ConnectionState.Connected ?: return
        currentState.binder.playWhenReady = !playWhenReady.value!!
    }
}
