package me.a0xcaff.forte.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.MergedLiveData
import me.a0xcaff.forte.default
import me.a0xcaff.forte.map
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.PlaybackServiceConnection

class BottomSheetViewModel(connection: PlaybackServiceConnection) : ViewModel() {
    val userState = MutableLiveData<@BottomSheetBehavior.State Int>().default(BottomSheetBehavior.STATE_HIDDEN)

    private val stateFromConnection = connection.state.map { connectionState ->
        when (connectionState) {
            is ConnectionState.Connected -> BottomSheetBehavior.STATE_COLLAPSED
            else -> BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private val _state = MergedLiveData(stateFromConnection, userState)

    val state: LiveData<Int> = _state
}
