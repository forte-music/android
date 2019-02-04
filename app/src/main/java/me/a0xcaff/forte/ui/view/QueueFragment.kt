package me.a0xcaff.forte.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import me.a0xcaff.forte.databinding.FragmentQueueBinding
import me.a0xcaff.forte.databinding.QueueItemBinding
import me.a0xcaff.forte.graphql.SongQueueQuery
import me.a0xcaff.forte.playback.*
import org.koin.android.ext.android.inject

// TODO: Dismissing Currently Playing Item Crashes
// TODO: Better Styling
// TODO: Skip to Position on Click
// TODO: Use Drag Handle Maybe
// TODO: Move Into Bottom Sheet

/**
 * Displays a UI for viewing, modifying, and skipping to a specific position in a [Queue].
 */
class QueueFragment : Fragment() {
    private val connection: PlaybackServiceConnection by inject()

    private lateinit var binding: FragmentQueueBinding

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQueueBinding.inflate(inflater, container, false)

        val queueAdapter = QueueAdapter(scope)

        val recyclerView = binding.items
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = queueAdapter
            setHasFixedSize(true)
        }

        queueAdapter.itemTouchHelper.attachToRecyclerView(recyclerView)

        connection.state.observe(this, Observer { connectionState ->
            when (connectionState) {
                is ConnectionState.Connected -> {
                    queueAdapter.queue = connectionState.binder.queue
                    connectionState.onUnbind.observe {
                        queueAdapter.queue = null
                    }
                }
            }
        })

        return binding.root
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

class QueueAdapter(
    val scope: CoroutineScope
) : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {
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

    class ViewHolder(val binding: QueueItemBinding, val scope: CoroutineScope) : RecyclerView.ViewHolder(binding.root) {
        private var currentItem: QueueItem? = null
        private var currentJob: Job? = null

        fun bind(item: QueueItem) {
            if (currentItem == item) {
                return
            }

            currentJob?.cancel()
            bind(loadDeferred(item.song))
            currentItem = item
        }

        private fun loadDeferred(deferred: Deferred<SongQueueQuery.Song>) =
            returnNowOrLater(scope, deferred, this::bind)

        private fun bind(song: SongQueueQuery.Song?) {
            binding.title.text = song?.name() ?: "..."
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = QueueItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, scope)
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

