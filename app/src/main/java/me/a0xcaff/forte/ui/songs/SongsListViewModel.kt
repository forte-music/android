package me.a0xcaff.forte.ui.songs

import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import me.a0xcaff.forte.playback.Backend
import me.a0xcaff.forte.playback.ConnectionState
import me.a0xcaff.forte.playback.PlaybackServiceConnection
import me.a0xcaff.forte.playback.QueueItem

class SongsListViewModel(
    private val backend: Backend,
    apolloClient: ApolloClient,
    private val connection: PlaybackServiceConnection
) : ViewModel() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val songsDataSource = SongsDataSourceFactory(coroutineScope, apolloClient)

    val songsList = songsDataSource
        .map { it.node() }
        .toLiveData(
            PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .build()
        )

    fun addSong(id: String) {
        val connectionState = connection.state.value!! as? ConnectionState.Connected ?: return
        val queue = connectionState.binder.queue
        queue.add(QueueItem(id, backend))
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }
}