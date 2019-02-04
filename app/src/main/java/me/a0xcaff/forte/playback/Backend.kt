package me.a0xcaff.forte.playback

import android.net.Uri
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import me.a0xcaff.forte.graphql.SongQueueQuery

class Backend(
    private val apolloClient: ApolloClient,
    private val baseUri: Uri,
    private val quality: Quality,
    private val scope: CoroutineScope
) {
    fun audioUri(songId: String): Uri = baseUri.buildUpon().run {
        listOf("files", "music", songId, quality.asPath())
            .forEach { appendEncodedPath(it) }

        build()
    }

    fun getQueueInfoAsync(songId: String): Deferred<SongQueueQuery.Song> =
        scope.async {
            val resp = apolloClient.query(
                SongQueueQuery.builder()
                    .id(songId)
                    .build()
            ).toDeferred().await()

            // TODO: Handle Error
            resp.data()!!.song()
        }
}

enum class Quality {
    RAW {
        override fun asPath(): String = "raw"
    };

    abstract fun asPath(): String
}
