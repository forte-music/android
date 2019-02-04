package me.a0xcaff.forte.playback

import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player

interface ObservableList<T> {
    val items: List<T>

    /**
     * Register an observer. Observers are triggered after changes are committed. Observers registered are triggered
     * first.
     */
    fun registerObserver(observer: Observer<T>)

    fun unregisterObserver(observer: Observer<T>)

    interface Observer<T> {
        /**
         * See [RecyclerView.Adapter.notifyItemMoved]
         */
        fun onItemMoved(fromPosition: Int, toPosition: Int)

        /**
         * See [RecyclerView.Adapter.notifyItemRemoved]
         */
        fun onItemRemoved(idx: Int)

        /**
         * See [RecyclerView.Adapter.notifyItemRangeInserted]
         *
         * @param items Items which were inserted.
         */
        fun onItemRangeInserted(positionStart: Int, items: List<T>)

        /**
         * See [RecyclerView.Adapter.notifyItemRangeChanged]
         *
         * @param items All items after [positionStart]
         */
        fun onItemRangeChanged(positionStart: Int, items: List<T>)
    }
}

abstract class BaseObservableList<T> : ObservableList<T> {
    private val observers: MutableList<ObservableList.Observer<T>> = mutableListOf()
    override fun registerObserver(observer: ObservableList.Observer<T>) {
        observers.add(observer)
    }

    override fun unregisterObserver(observer: ObservableList.Observer<T>) {
        observers.remove(observer)
    }

    fun notifyItemMoved(fromPosition: Int, toPosition: Int) =
        observers.forEach { it.onItemMoved(fromPosition, toPosition) }

    fun notifyItemRemoved(idx: Int) = observers.forEach { it.onItemRemoved(idx) }

    fun notifyItemRangeInserted(positionStart: Int, count: Int) {
        val newItems = items.slice(positionStart until (positionStart + count))
        observers.forEach { it.onItemRangeInserted(positionStart, newItems) }
    }

    fun notifyItemRangeChanged(positionStart: Int, count: Int) {
        val newItems = items.slice(positionStart until (positionStart + count))
        observers.forEach { it.onItemRangeChanged(positionStart, newItems) }
    }
}

interface MutableObservableList<T> : ObservableList<T> {
    fun add(vararg items: T) {
        insert(Math.max(0, this.items.lastIndex), *items)
    }

    fun remove(idx: Int): T
    fun move(fromPosition: Int, toPosition: Int)
    fun insert(idx: Int, vararg items: T)
    fun shuffleStartingFrom(startIdx: Int)
}

class Queue : BaseObservableList<QueueItem>(), MutableObservableList<QueueItem> {
    private val _items: MutableList<QueueItem> = mutableListOf()
    override val items: List<QueueItem> = _items

    override fun remove(idx: Int): QueueItem {
        val removed = _items.removeAt(idx)
        notifyItemRemoved(idx)
        return removed
    }

    override fun move(fromPosition: Int, toPosition: Int) {
        val start = Math.min(fromPosition, toPosition)
        val end = Math.max(fromPosition, toPosition)

        for (idx in start until end) {
            _items.swap(idx, idx + 1)
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    override fun insert(idx: Int, vararg items: QueueItem) {
        _items.addAll(idx, items.toList())
        notifyItemRangeInserted(idx, items.size)
    }

    override fun shuffleStartingFrom(startIdx: Int) {
        TODO("not implemented")
    }

    fun unshuffle(startIdx: Int) {
        TODO("not implemented")
    }

    fun getNowPlaying(player: Player): QueueItem? = items.getOrNull(player.currentWindowIndex)
}

fun <T> MutableList<T>.swap(fromIndex: Int, toIndex: Int) {
    val tmp = this[fromIndex]
    this[fromIndex] = this[toIndex]
    this[toIndex] = tmp
}
