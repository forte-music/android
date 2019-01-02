package me.a0xcaff.forte.ui.view

import androidx.recyclerview.widget.RecyclerView
import me.a0xcaff.forte.playback.ObservableList

/**
 * Notifies a [RecyclerView.Adapter] of changes from an [ObservableList.Observer].
 */
class RecyclerViewListener<T>(private val adapter: RecyclerView.Adapter<*>) : ObservableList.Observer<T> {
    override fun onItemMoved(fromPosition: Int, toPosition: Int) = adapter.notifyItemMoved(fromPosition, toPosition)

    override fun onItemRemoved(idx: Int) = adapter.notifyItemRemoved(idx)

    override fun onItemRangeInserted(positionStart: Int, items: List<T>) =
        adapter.notifyItemRangeInserted(positionStart, items.size)

    override fun onItemRangeChanged(positionStart: Int, items: List<T>) =
        adapter.notifyItemRangeChanged(positionStart, items.size)
}
