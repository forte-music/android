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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import me.a0xcaff.forte.R
import me.a0xcaff.forte.playback.ConnectionState

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
    private val styled: TypedArray? = context.obtainStyledAttributes(attrs, R.styleable.PlaybackProgressBar)

    private val bufferedPaint = Paint().apply {
        color = styled.getColor(R.styleable.PlaybackProgressBar_buffered_color, Color.BLUE)
    }

    private val playedPaint = Paint().apply {
        color = styled.getColor(R.styleable.PlaybackProgressBar_played_color, Color.RED)
    }

    private var bufferedProgress = styled.getFloat(R.styleable.PlaybackProgressBar_buffered_progress, 0.0f)
    private var playedProgress = styled.getFloat(R.styleable.PlaybackProgressBar_played_progress, 0.0f)

    private val manager = ServiceRegistrationManager(
        onBound = { service ->
            service.playbackStateChanged.observe(this::handlePlaybackStateChanged)
            postUpdateOnAnimation()
        },
        onUnbound = { service ->
            service.playbackStateChanged.unObserve(this::handlePlaybackStateChanged)
        }
    )

    fun register(liveData: LiveData<ConnectionState>, lifecycleOwner: LifecycleOwner) =
        manager.register(liveData, lifecycleOwner)

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

    @Suppress("UNUSED_PARAMETER")
    private fun handlePlaybackStateChanged(unit: Unit) = handlePlaybackStateChanged()

    private fun handlePlaybackStateChanged() {
        if (manager.isBound) {
            postUpdateOnAnimation()
        }
    }

    private fun postUpdateOnAnimation() {
        postOnAnimation {
            if (manager.isBound) {
                updateProgress()
                invalidate()

                postUpdateOnAnimation()
            }
        }
    }

    private fun updateProgress() {
        val activeBinder = manager.binder
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
