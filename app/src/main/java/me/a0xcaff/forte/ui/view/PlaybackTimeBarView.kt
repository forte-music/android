package me.a0xcaff.forte.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.ui.ServiceRegistrationManager

class PlaybackTimeBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : DefaultTimeBar(context, attrs) {
    private val manager = ServiceRegistrationManager(
        onBound = { service ->
            service.queuePositionChanged.observe(this::handleUpdate)
            postUpdateOnAnimation()
        },
        onUnbound = { service ->
            service.queuePositionChanged.unObserve(this::handleUpdate)
        }
    )

    private val listener = Listener()

    init {
        addListener(listener)
    }

    fun register(liveData: LiveData<ConnectionState>, lifecycleOwner: LifecycleOwner) =
        manager.register(liveData, lifecycleOwner)

    @Suppress("UNUSED_PARAMETER")
    private fun handleUpdate(unit: Unit) =
        handleUpdate()

    private fun handleUpdate() {
        if (manager.isBound) {
            updatePositions()
        }
    }

    private fun postUpdateOnAnimation() {
        postOnAnimation {
            if (manager.isBound) {
                updatePositions()
                postUpdateOnAnimation()
            }
        }
    }

    private fun updatePositions() {
        val service = manager.mustBeBound()
        setPosition(service.nowPlaying?.currentPosition ?: 0)
        setBufferedPosition(service.nowPlaying?.bufferedPosition ?: 0)
        setDuration(service.nowPlaying?.duration ?: 0)
    }

    inner class Listener : TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {
        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            manager.mustBeBound().nowPlaying?.currentPosition = position
        }
    }
}
