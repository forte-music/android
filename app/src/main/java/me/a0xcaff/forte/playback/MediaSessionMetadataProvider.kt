package me.a0xcaff.forte.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class MediaSessionMetadataProvider(
    private val queue: Queue,
    private val fetcher: BitmapFetcher
) : MediaSessionConnector.MediaMetadataProvider {
    override fun getMetadata(player: Player): MediaMetadataCompat {
        val playing = queue.getNowPlaying(player)
        return MediaMetadataCompat.Builder().apply {
            if (playing == null) {
                return@apply
            }

            val bitmap = fetcher.getFor(playing.album.artworkUrl) {
                connector!!.invalidateMediaSessionMetadata()
            }

            putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, playing.title)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playing.album.title)
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, playing.songId)
            putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                playing.artists.joinToString(", ") { it.name }
            )

            // TODO: Additional Metadata
        }.build()
    }

    var connector: MediaSessionConnector? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Connector already configured")
            }

            field = value
        }

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
