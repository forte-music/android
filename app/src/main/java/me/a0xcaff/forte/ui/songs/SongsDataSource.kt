package me.a0xcaff.forte.ui.songs

import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.CoroutineScope
import me.a0xcaff.forte.graphql.SongsListQuery
import me.a0xcaff.forte.ui.AsyncItemKeyedDataSource

class SongsDataSource(
    scope: CoroutineScope,
    private val apolloClient: ApolloClient
) : AsyncItemKeyedDataSource<String, SongsListQuery.Edge>(scope) {
    override suspend fun loadInitial(params: LoadInitialParams<String>): LoadInitialResult<SongsListQuery.Edge> {
        val response = apolloClient.query(
            SongsListQuery.builder()
                .after(params.requestedInitialKey)
                .pageSize(params.requestedLoadSize)
                .build()
        )
            .toDeferred()
            .await()

        // TODO: Handle Error
        val data = response.data()!!

        return LoadInitialResult(
            data = data.songs().edges(),
            totalCount = data.songs().count()
        )
    }

    override suspend fun loadAfter(params: LoadParams<String>): List<SongsListQuery.Edge> {
        val response = apolloClient.query(
            SongsListQuery.builder()
                .after(params.key)
                .pageSize(params.requestedLoadSize)
                .build()
        )
            .toDeferred()
            .await()

        // TODO: Handle Error
        val data = response.data()!!

        // TODO: Use hasNextPage Somehow

        return data.songs().edges()
    }

    override fun getKey(item: SongsListQuery.Edge): String =
        item.cursor()
}

class SongsDataSourceFactory(
    private val scope: CoroutineScope,
    private val apolloClient: ApolloClient
) : DataSource.Factory<String, SongsListQuery.Edge>() {
    override fun create(): DataSource<String, SongsListQuery.Edge> =
        SongsDataSource(scope, apolloClient)
}

