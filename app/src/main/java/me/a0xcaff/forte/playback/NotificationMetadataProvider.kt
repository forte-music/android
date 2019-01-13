package me.a0xcaff.forte.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.google.android.exoplayer2.Player
import me.a0xcaff.forte.R
import me.a0xcaff.forte.ui.view.ViewActivity

class NotificationMetadataProvider(
    private val queue: Queue,
    private val context: Context,
    private val fetcher: BitmapFetcher
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): String = queue.getNowPlaying(player)!!.title

    override fun getCurrentContentText(player: Player): String {
        val playing = queue.getNowPlaying(player)!!

        return "${playing.album.title} • ${playing.artists.joinToString(", ") { it.name }}"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent {
        val intent = Intent(context, ViewActivity::class.java).apply {
            putExtras(ViewActivity.Extras.OpenNowPlaying.build())
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? =
        fetcher.getFor(
            queue.getNowPlaying(player)!!.album.artworkUrl,
            {
                resizeDimen(R.dimen.notification_large_artwork_size, R.dimen.notification_large_artwork_size)
                centerInside()
            },
            callback::onBitmap
        )
}
