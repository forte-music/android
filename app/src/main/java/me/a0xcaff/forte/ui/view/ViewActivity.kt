package me.a0xcaff.forte.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.ActivityViewBinding
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.ui.dataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val EXTRA_TYPE_KEY = "type"

class ViewActivity : AppCompatActivity() {
    private val binding: ActivityViewBinding by dataBinding(R.layout.activity_view)

    private val bottomSheetViewModel: BottomSheetViewModel by viewModel()

    private val playbackViewModel: PlaybackViewModel by viewModel()

    private lateinit var bottomSheetBehavior: PlaybackBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bottomSheetBehavior = PlaybackBottomSheet(
            binding,
            resources.getDimension(R.dimen.view_bottom_sheet_peek).toInt(),
            bottomSheetViewModel.userState::setValue
        )

        bottomSheetViewModel.state.observe(this, Observer(bottomSheetBehavior::state::set))

        binding.sheetPeek.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }

        binding.playbackProgress.register(playbackViewModel.connection.state, this)

        binding.playPause.setOnClickListener { playbackViewModel.togglePlayWhenReady() }
        playbackViewModel.playWhenReady.observe(this, Observer(binding.playPause::updatePlaybackState))

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

