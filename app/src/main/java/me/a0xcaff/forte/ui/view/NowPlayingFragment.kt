package me.a0xcaff.forte.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import me.a0xcaff.forte.databinding.FragmentNowPlayingBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

// TODO: Image Placeholder

class NowPlayingFragment : Fragment() {
    private lateinit var binding: FragmentNowPlayingBinding
    private val viewModel: PlaybackViewModel by sharedViewModel()
    private val picasso: Picasso by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNowPlayingBinding.inflate(inflater, container, false)

        binding.next.setOnClickListener { viewModel.skipForwards() }
        viewModel.canSkipForwards.observe(this, Observer(binding.next::setEnabled))

        binding.previous.setOnClickListener { viewModel.skipBackwards() }

        binding.playPause.setOnClickListener { viewModel.togglePlayWhenReady() }
        viewModel.playWhenReady.observe(this, Observer(binding.playPause::updatePlaybackState))

        viewModel.nowPlaying.observe(this, Observer { song ->
            if (song == null) {
                binding.album.text = "..."
                binding.artist.text = "..."
                binding.artwork.setImageBitmap(null)
                return@Observer
            }

            val artworkUrl = song.album().artworkUrl()
            if (artworkUrl != null) {
                picasso.load(artworkUrl).into(binding.artwork)
            }

            binding.album.text = song.album().name()
            binding.artist.text = song.artists().joinToString(", ") { it.name() }
        })

        binding.currentTime.register(viewModel.connection.state, this)
        viewModel.duration.observe(this, Observer { binding.totalTime.text = it })
        binding.timebar.register(viewModel.connection.state, this)

        // Don't allow touch event to bubble behind this fragment.
        binding.root.setOnTouchListener { _, _ -> true }

        return binding.root
    }
}
