package me.a0xcaff.forte.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.ActivityViewBinding
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.PlaybackState
import me.a0xcaff.forte.ui.dataBinding

// TODO: Better Connect Service Lifecycle View Model Maybe
// TODO: Put Bottom Sheet State in View Model

private const val EXTRA_TYPE_KEY = "type"

class ViewActivity : AppCompatActivity() {
    val binding: ActivityViewBinding by dataBinding(R.layout.activity_view)

    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(binding.sheet) }

    private lateinit var connection: PlaybackServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connection = PlaybackServiceConnection(this).apply {
            state.observe(this@ViewActivity, Observer { connectionState ->
                when (connectionState) {
                    is ConnectionState.Connected -> {
                        binding.playPause.setOnClickListener {
                            connectionState.binder.playWhenReady = !connectionState.binder.playWhenReady
                        }

                        connectionState.onUnbind.observe {
                            binding.playPause.setOnClickListener(null)
                        }

                        updatePlaybackState(connectionState.binder.state, connectionState.binder.playWhenReady)
                        val observer: (Unit) -> Unit = {
                            updatePlaybackState(connectionState.binder.state, connectionState.binder.playWhenReady)
                        }

                        connectionState.binder.playbackStateChanged.observe(observer)

                        connectionState.onUnbind.observe {
                            connectionState.binder.playbackStateChanged.unObserve(observer)
                        }

                        binding.playbackProgress.registerBinder(connectionState.binder)
                        connectionState.onUnbind.observe { binding.playbackProgress.unregisterBinder() }
                    }
                }
            })
        }

        val peek = binding.sheetPeek
        val content = binding.sheetContent

        binding.sheetPeek.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }

        val peekHeightAsPercent = 1.0f / 8.0f
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val progress = Math.min(slideOffset / peekHeightAsPercent, 1.0f)

                content.alpha = progress
                peek.alpha = 1 - progress
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        content.visibility = View.GONE
                    BottomSheetBehavior.STATE_EXPANDED ->
                        peek.visibility = View.GONE
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        peek.visibility = View.GONE
                        content.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_SETTLING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        peek.visibility = View.VISIBLE
                        content.visibility = View.VISIBLE
                    }
                }
            }
        })

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) =
        handleIntent(intent)

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        return super.onBackPressed()
    }

    private fun handleIntent(intent: Intent) {
        val extras = Extras.from(intent.extras)

        when (extras) {
            is Extras.OpenNowPlaying -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            is Extras.NoArgs -> {
                // Do nothing.
            }
        }
    }

    private fun updatePlaybackState(state: PlaybackState, playWhenReady: Boolean) {
        when {
            state is PlaybackState.Ready && playWhenReady -> binding.playPause.setPauseImage()
            state is PlaybackState.Ready && !playWhenReady -> binding.playPause.setPlayImage()
            else -> binding.playPause.clearImage()
        }
    }

    override fun onStart() {
        super.onStart()
        connection.bind()
    }

    override fun onStop() {
        super.onStop()
        connection.unbind()
    }

    sealed class Extras {
        object OpenNowPlaying : Extras()
        object NoArgs : Extras()

        fun build(): Bundle =
            when (this) {
                is OpenNowPlaying -> Bundle().apply {
                    putString(EXTRA_TYPE_KEY, OpenNowPlaying::javaClass.name)
                }
                is NoArgs -> Bundle()
            }

        companion object {
            fun from(bundle: Bundle?): Extras =
                when (bundle?.getString(EXTRA_TYPE_KEY)) {
                    null -> NoArgs
                    OpenNowPlaying::javaClass.name -> OpenNowPlaying
                    else -> throw IllegalArgumentException("invalid bundle")
                }
        }
    }
}

