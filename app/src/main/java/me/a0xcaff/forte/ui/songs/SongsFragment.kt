package me.a0xcaff.forte.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.a0xcaff.forte.databinding.FragmentSongsBinding
import me.a0xcaff.forte.databinding.QueueItemBinding
import me.a0xcaff.forte.graphql.SongsListQuery
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SongsFragment : Fragment() {
    private lateinit var binding: FragmentSongsBinding

    private val viewModel: SongsListViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSongsBinding.inflate(inflater, container, false)

        val listAdapter = SongsAdapter(viewModel::addSong)
        binding.songsList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = listAdapter
        }

        viewModel.songsList.observe(this, Observer { listAdapter.submitList(it) })

        return binding.root
    }
}

class SongsAdapter(
    private val onAddSong: (id: String) -> Unit
) : PagedListAdapter<SongsListQuery.Node, ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.create(LayoutInflater.from(parent.context), parent, onAddSong)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SongsListQuery.Node>() {
        override fun areItemsTheSame(oldItem: SongsListQuery.Node, newItem: SongsListQuery.Node): Boolean =
            oldItem.id() == newItem.id()

        override fun areContentsTheSame(oldItem: SongsListQuery.Node, newItem: SongsListQuery.Node): Boolean =
            true
    }
}

class ViewHolder(
    val onAddSong: (id: String) -> Unit,
    val binding: QueueItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: SongsListQuery.Node?) {
        binding.title.text = data?.name() ?: "Loading..."

        binding.root.setOnClickListener {
            data ?: return@setOnClickListener

            onAddSong(data.id())
        }
    }

    companion object {
        fun create(layoutInflater: LayoutInflater, root: ViewGroup, onAddSong: (id: String) -> Unit): ViewHolder =
            ViewHolder(
                onAddSong,
                QueueItemBinding.inflate(layoutInflater, root, false)
            )
    }
}

