package me.a0xcaff.forte.ui.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.ActivityViewBinding
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.PlaybackState

// TODO: Display Queue
// TODO: MotionLayout

class ViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding

    private lateinit var connection: PlaybackServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view)
        connection = PlaybackServiceConnection(this) { service, lifecycle ->
            binding.playPause.setOnClickListener {
                service.playWhenReady = !service.playWhenReady
            }

            lifecycle.registerOnUnbind {
                binding.playPause.setOnClickListener(null)
            }

            updatePlaybackState(service.state, service.playWhenReady)
            val observer: (Unit) -> Unit = {
                updatePlaybackState(service.state, service.playWhenReady)
            }

            service.playbackStateChanged.observe(observer)

            lifecycle.registerOnUnbind {
                service.playbackStateChanged.unObserve(observer)
            }
        }

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
        connection.tryUnbind()
    }
}
