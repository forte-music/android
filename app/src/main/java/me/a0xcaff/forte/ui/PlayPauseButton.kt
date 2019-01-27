package me.a0xcaff.forte.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import me.a0xcaff.forte.R

class PlayPauseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {
    private fun setPlayImage() {
        setImageResource(R.drawable.exo_controls_play)
    }

    private fun setPauseImage() {
        setImageResource(R.drawable.exo_controls_pause)
    }

    fun clearImage() {
        setImageDrawable(null)
    }

    fun updatePlaybackState(playWhenReady: Boolean) {
        when {
            playWhenReady -> setPauseImage()
            !playWhenReady -> setPlayImage()
        }
    }

}
