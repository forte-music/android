package me.a0xcaff.forte.playback

import android.net.Uri

// TODO: Fetch Data from Apollo
// TODO: Figure Out Threading

var lastQueueId: Long = 0

enum class Quality {
    RAW {
        override fun asPath(): String = "raw"
    };

    abstract fun asPath(): String
}

class Album(
    val id: String,
    val title: String,
    val artworkUrl: String
)

class Artist(
    val name: String
)

class QueueItem(
    val songId: String,
    val title: String,
    val artists: Array<Artist>,
    val album: Album
) {
    val id: Long = lastQueueId++

    fun audioUri(base: Uri, quality: Quality): Uri = base.buildUpon().run {
        appendEncodedPath("files")
        appendEncodedPath("music")
        appendEncodedPath(songId)
        appendEncodedPath(quality.asPath())

        build()
    }
}

