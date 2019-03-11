package me.a0xcaff.forte.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.a0xcaff.forte.databinding.FragmentSongsBinding

class SongsFragment : Fragment() {
    private lateinit var binding: FragmentSongsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSongsBinding.inflate(inflater, container, false)

        return binding.root
    }
}