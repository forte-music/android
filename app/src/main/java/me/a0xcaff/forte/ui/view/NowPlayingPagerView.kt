package me.a0xcaff.forte.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import me.a0xcaff.forte.databinding.ItemNowPlayingPageBinding
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.PlaybackServiceBinder
import me.a0xcaff.forte.playback.QueueItem
import me.a0xcaff.forte.ui.ServiceRegistrationManager

class NowPlayingPagerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val manager = ServiceRegistrationManager(
        onBound = { service ->
            service.queuePositionChanged.observe(this::handleQueuePositionChangedFromService)
            handleQueuePositionChangedFromService(Unit)
            adapter = NowPlayingInfoAdapter(service)
        },
        onUnbound = { service ->
            service.queuePositionChanged.unObserve(this::handleQueuePositionChangedFromService)
            adapter = null
        }
    )

    private val layoutManager = ScrollForceLayoutManager(context)
    private val snapHelper = PagerSnapHelper()
    private val scrollListener = ScrollListener()
    private var adapter: NowPlayingInfoAdapter? = null
        set(value) {
            field?.release()
            setAdapter(value)
            field = value
        }

    init {
        addOnScrollListener(scrollListener)
        setLayoutManager(layoutManager)
        setHasFixedSize(true)
        snapHelper.attachToRecyclerView(this)
    }

    fun register(liveData: LiveData<ConnectionState>, lifecycleOwner: LifecycleOwner) =
        manager.register(liveData, lifecycleOwner)

    @Suppress("UNUSED_PARAMETER")
    private fun handleQueuePositionChangedFromService(arg: Unit) =
        handleQueuePositionChangedFromService()

    private fun handleQueuePositionChangedFromService() {
        // TODO: Handle Edge Cases
        val service = manager.mustBeBound()

        if (!layoutManager.isSmoothScrolling) {
            layoutManager.smoothScrollToPosition(this, RecyclerView.State(), service.nowPlayingIndex)
        }
    }

    inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> maybeUpdateNowPlaying()
            }
        }

        private fun maybeUpdateNowPlaying() {
            val view = snapHelper.findSnapView(layoutManager) ?: return
            val position = layoutManager.getPosition(view)
            val currentPlayingIndex = manager.binder?.nowPlayingIndex
            if (currentPlayingIndex != position) {
                manager.binder?.nowPlayingIndex = position
            }
        }
    }
}

class ScrollForceLayoutManager(
    context: Context,
    private val forceFactor: Double = 1.3
) : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int =
        super.scrollHorizontallyBy((dx / forceFactor).toInt(), recycler, state)
}

class NowPlayingInfoAdapter(
    private val service: PlaybackServiceBinder
) : RecyclerView.Adapter<NowPlayingInfoAdapter.ViewHolder>() {
    val listener = RecyclerViewListener<QueueItem>(this)

    init {
        service.queue.registerObserver(listener)
    }

    class ViewHolder(val binding: ItemNowPlayingPageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QueueItem) {
            binding.title.text = "${item.album.title} • ${item.artists.joinToString(", ") { it.name }}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemNowPlayingPageBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(service.queue.items[position])

    override fun getItemCount(): Int =
        service.queue.items.size

    fun release() {
        service.queue.unregisterObserver(listener)
    }
}
