package me.a0xcaff.forte.playback

import android.net.Uri
import kotlinx.coroutines.Deferred
import me.a0xcaff.forte.graphql.SongQueueQuery

var lastQueueId: Long = 0

class QueueItem(
    val songId: String,
    private val backend: Backend
) {
    val id: Long = lastQueueId++

    val song: Deferred<SongQueueQuery.Song> by lazy { backend.getQueueInfoAsync(songId) }

    val songUri: Uri = backend.audioUri(songId)
}
