package me.a0xcaff.forte.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.a0xcaff.forte.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {
    private lateinit var binding: FragmentNowPlayingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNowPlayingBinding.inflate(inflater, container, false)

        return binding.root
    }
}
