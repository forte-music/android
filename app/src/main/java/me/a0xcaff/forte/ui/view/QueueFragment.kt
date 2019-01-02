package me.a0xcaff.forte.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.a0xcaff.forte.databinding.FragmentQueueBinding
import me.a0xcaff.forte.databinding.QueueItemBinding
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.Queue
import me.a0xcaff.forte.playback.QueueItem

// TODO: Dismissing Currently Playing Item Crashes
// TODO: Better Styling
// TODO: Skip to Position on Click
// TODO: Use Drag Handle Maybe
// TODO: Move Into Bottom Sheet

/**
 * Displays a UI for viewing, modifying, and skipping to a specific position in a [Queue].
 */
class QueueFragment : Fragment() {
    private lateinit var connection: PlaybackServiceConnection

    private lateinit var binding: FragmentQueueBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQueueBinding.inflate(inflater, container, false)

        val queueAdapter = QueueAdapter()

        val recyclerView = binding.items
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = queueAdapter
            setHasFixedSize(true)
        }

        queueAdapter.itemTouchHelper.attachToRecyclerView(recyclerView)

        connection = PlaybackServiceConnection(context!!) { service, lifecycle ->
            queueAdapter.queue = service.queue
            lifecycle.registerOnUnbind {
                queueAdapter.queue = null
            }
        }

        return binding.root
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

class QueueAdapter : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {
    private val itemTouchCallback = ItemTouchCallback()

    val itemTouchHelper = ItemTouchHelper(itemTouchCallback)

    init {
        setHasStableIds(true)
    }

    val listener = RecyclerViewListener<QueueItem>(this)

    var queue: Queue? = null
        set(value) {
            field?.unregisterObserver(listener)
            value?.registerObserver(listener)

            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(val binding: QueueItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QueueItem) {
            binding.title.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = QueueItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(queue!!.items[position])

    override fun getItemId(position: Int): Long = queue!!.items[position].id

    override fun getItemCount(): Int = queue?.items?.size ?: 0

    inner class ItemTouchCallback : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            queue?.move(viewHolder.adapterPosition, target.adapterPosition)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            queue?.remove(viewHolder.adapterPosition)
        }
    }
}

