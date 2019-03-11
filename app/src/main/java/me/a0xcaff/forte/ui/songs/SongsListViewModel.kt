package me.a0xcaff.forte.ui.songs

import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel

class SongsListViewModel(
    apolloClient: ApolloClient
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

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }
}