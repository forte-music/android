package me.a0xcaff.forte.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class MediaSessionMetadataProvider(
    private val queue: Queue,
    private val fetcher: BitmapFetcher
) : MediaSessionConnector.MediaMetadataProvider {
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun getMetadata(player: Player): MediaMetadataCompat {
        val playing = queue.getNowPlaying(player)
        return MediaMetadataCompat.Builder().apply {
            if (playing == null) {
                return@apply
            }

            val song = loadDeferred(playing.song) ?: return@apply

            val artworkUrl = song.album().artworkUrl()
            val bitmap = artworkUrl?.let { loadDeferred(fetcher.getAsync(it)) }

            putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.name())
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album().name())
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, playing.songId)
            putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                song.artists().joinToString(", ") { it.name() }
            )

            // TODO: Additional Metadata
        }.build()
    }

    private fun <T> loadDeferred(deferred: Deferred<T>) =
        returnNowOrLater(scope, deferred) { connector.invalidateMediaSessionMetadata() }

    fun release() {
        scope.cancel()
    }

    lateinit var connector: MediaSessionConnector

    companion object {
        fun withConnector(
            mediaSession: MediaSessionCompat,
            queue: Queue,
            fetcher: BitmapFetcher
        ): MediaSessionConnector {
            val metadataProvider = MediaSessionMetadataProvider(queue, fetcher)

            val connector = MediaSessionConnector(
                mediaSession,
                null,
                metadataProvider
            )

            metadataProvider.connector = connector

            return connector
        }
    }
}
