package me.a0xcaff.forte.ui.view

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.Config
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.ActivityViewBinding
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import org.koin.android.ext.android.inject

class ViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding

    private lateinit var connection: PlaybackServiceConnection

    private val config: Config by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view)
        connection = PlaybackServiceConnection(this) { lifecycle ->
            binding.playPause.setOnClickListener {
                if (binding.playPause.isPlaying) {
                    lifecycle.service.mediaController.transportControls.pause()
                } else {
                    lifecycle.service.mediaController.transportControls.play()
                }
            }
            lifecycle.registerOnUnbind {
                binding.playPause.setOnClickListener(null)
            }

            updatePlaybackState(lifecycle.service.mediaController.playbackState)
            val callback =
                object : MediaControllerCompat.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                        updatePlaybackState(state)
                    }
                }

            lifecycle.service.mediaController.registerCallback(callback)
            lifecycle.registerOnUnbind {
                lifecycle.service.mediaController.unregisterCallback(callback)
            }
        }

        binding.serverUrl.text = config.serverUrl.toString()

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet)
        val peek = binding.sheetPeek
        val content = binding.sheetContent

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
    }

    fun updatePlaybackState(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            binding.playPause.isPlaying = true
        }

        if (state.state == PlaybackStateCompat.STATE_PAUSED) {
            binding.playPause.isPlaying = false
        }
    }

    override fun onStart() {
        super.onStart()
        connection.bind()
    }

    override fun onPause() {
        super.onPause()
        connection.tryUnbind()
    }
}
