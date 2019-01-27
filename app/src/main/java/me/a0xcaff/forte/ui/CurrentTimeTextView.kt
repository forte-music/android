package me.a0xcaff.forte.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import me.a0xcaff.forte.R
import me.a0xcaff.forte.playback.ConnectionState

class CurrentTimeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val manager = ServiceRegistrationManager(
        onBound = { service ->
            service.queuePositionChanged.observe(this::handleQueuePositionChanged)
            postUpdateOnAnimation()
        },
        onUnbound = { service ->
            service.queuePositionChanged.unObserve(this::handleQueuePositionChanged)
        }
    )

    fun register(liveData: LiveData<ConnectionState>, lifecycleOwner: LifecycleOwner) =
        manager.register(liveData, lifecycleOwner)

    @Suppress("UNUSED_PARAMETER")
    private fun handleQueuePositionChanged(unit: Unit) =
        handleQueuePositionChanged()

    private fun handleQueuePositionChanged() {
        if (manager.isBound) {
            postUpdateOnAnimation()
        }
    }

    private fun postUpdateOnAnimation() {
        postOnAnimation {
            if (manager.isBound) {
                updateText()
                postUpdateOnAnimation()
            }
        }
    }

    private fun updateText() {
        val time = manager.binder?.nowPlaying?.currentPosition
        if (time == null) {
            text = context.getText(R.string.default_duration)
        } else {
            text = formatTime(time)
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val millisecondsInSecond = 1000
    val minutesInHour = 60
    val secondsInMinute = 60
    val secondsInHour = secondsInMinute * minutesInHour

    val totalSeconds = milliseconds / millisecondsInSecond

    val hours = totalSeconds / secondsInHour
    val minutes = (totalSeconds - (hours * secondsInHour)) / secondsInMinute
    val seconds = (totalSeconds - (hours * secondsInHour) - (minutes * secondsInMinute))

    return if (hours == 0L) {
        "$minutes:${seconds.asTrailingTimeString()}"
    } else {
        "$hours:${minutes.asTrailingTimeString()}:${seconds.asTrailingTimeString()}"
    }
}

fun Long.asTrailingTimeString() =
    toString().padStart(2, '0')

