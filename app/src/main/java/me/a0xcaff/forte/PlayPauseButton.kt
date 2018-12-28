package me.a0xcaff.forte

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton

class PlayPauseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr) {
    public var isPlaying: Boolean = true
        set(value) {
            field = value
            isPlayingChanged()
        }

    init {
        isPlayingChanged()
    }

    private fun isPlayingChanged() {
        val resource = when (isPlaying) {
            true -> R.drawable.exo_controls_pause
            false -> R.drawable.exo_controls_play
        }

        setImageResource(resource)
    }
}
