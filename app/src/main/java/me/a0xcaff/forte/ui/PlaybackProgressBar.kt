package me.a0xcaff.forte.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import me.a0xcaff.forte.R
import me.a0xcaff.forte.playback.PlaybackServiceBinder

// TODO: Display Buffering State

/**
 * Displays the buffered and played progress of playback. Updates on every frame while bound. Accepts the following
 * attributes.
 *
 * Attributes:
 *   * `buffered_progress`/`played_progress` Current progress of each bar. Useful for layout editor preview.
 *   * `buffered_color`/`played_color` Color of each of the bars. The played bar is drawn over the buffered bar.
 */
class PlaybackProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var binder: PlaybackServiceBinder? = null
    private val styled: TypedArray? = context.obtainStyledAttributes(attrs, R.styleable.PlaybackProgressBar)

    private val bufferedPaint = Paint().apply {
        color = styled.getColor(R.styleable.PlaybackProgressBar_buffered_color, Color.BLUE)
    }

    private val playedPaint = Paint().apply {
        color = styled.getColor(R.styleable.PlaybackProgressBar_played_color, Color.RED)
    }

    private var bufferedProgress = styled.getFloat(R.styleable.PlaybackProgressBar_buffered_progress, 0.0f)
    private var playedProgress = styled.getFloat(R.styleable.PlaybackProgressBar_played_progress, 0.0f)

    fun registerBinder(service: PlaybackServiceBinder) {
        service.playbackStateChanged.observe(this::handlePlaybackStateChanged)
        binder = service
        postUpdateOnAnimation()
    }

    fun unregisterBinder() {
        val activeBinder = mustBeBound()
        activeBinder.playbackStateChanged.unObserve(this::handlePlaybackStateChanged)
        binder = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawRect(canvas, bufferedProgress, bufferedPaint)
        drawRect(canvas, playedProgress, playedPaint)
    }

    private fun drawRect(canvas: Canvas, progress: Float, paint: Paint) {
        canvas.drawRect(
            0.0f,
            0.0f,
            width.toFloat() * progress,
            height.toFloat(),
            paint
        )
    }

    private fun mustBeBound(): PlaybackServiceBinder =
        binder ?: throw IllegalStateException("the PlaybackProgressBar must be registered first.")

    @Suppress("UNUSED_PARAMETER")
    private fun handlePlaybackStateChanged(unit: Unit) = handlePlaybackStateChanged()

    private fun isActive(): Boolean {
        val activeBinder = binder
        return activeBinder != null
    }

    private fun handlePlaybackStateChanged() {
        if (isActive()) {
            postUpdateOnAnimation()
        }
    }

    private fun postUpdateOnAnimation() {
        postOnAnimation {
            updateProgress()
            invalidate()

            if (isActive()) {
                postUpdateOnAnimation()
            }
        }
    }

    private fun updateProgress() {
        val activeBinder = binder
        val nowPlaying = activeBinder?.nowPlaying
        if (nowPlaying == null || nowPlaying.duration == 0L) {
            playedProgress = 0.0f
            bufferedProgress = 0.0f
        } else {
            bufferedProgress = nowPlaying.bufferedPosition.toFloat() / nowPlaying.duration.toFloat()
            playedProgress = nowPlaying.currentPosition.toFloat() / nowPlaying.duration.toFloat()
        }
    }
}

@ColorInt
fun TypedArray?.getColor(@StyleableRes resId: Int, @ColorInt default: Int): Int =
    this?.getColor(resId, default) ?: default

fun TypedArray?.getFloat(@StyleableRes resId: Int, default: Float): Float = this?.getFloat(resId, default) ?: default
