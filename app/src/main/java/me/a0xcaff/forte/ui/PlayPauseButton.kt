package me.a0xcaff.forte.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import me.a0xcaff.forte.R

class PlayPauseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr) {
    fun setPlayImage() {
        setImageResource(R.drawable.exo_controls_play)
    }

    fun setPauseImage() {
        setImageResource(R.drawable.exo_controls_pause)
    }

    fun clearImage() {
        setImageDrawable(null)
    }
}
