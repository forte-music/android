package me.a0xcaff.forte.ui.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.FragmentNavigationRootBinding

class NavRootFragment : Fragment() {
    private lateinit var binding: FragmentNavigationRootBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNavigationRootBinding.inflate(inflater, container, false)

        val navController = findNavController()
        binding.queue.setOnClickListener { navController.navigate(R.id.action_root_to_queue) }

        return binding.root
    }
}
