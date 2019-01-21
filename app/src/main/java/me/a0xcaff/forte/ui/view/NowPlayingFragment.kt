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

// TODO: Implement Playback Time View + Seekbar
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

        viewModel.nowPlaying.observe(this, Observer {
            picasso.load(it.item.album.artworkUrl)
                .into(binding.artwork)

            binding.album.text = it.item.album.title
            binding.artist.text = it.item.artists.joinToString(", ") { it.name }
        })

        return binding.root
    }
}
