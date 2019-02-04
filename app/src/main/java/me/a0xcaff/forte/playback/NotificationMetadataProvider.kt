package me.a0xcaff.forte.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import me.a0xcaff.forte.SingleValueCache
import me.a0xcaff.forte.graphql.SongQueueQuery
import me.a0xcaff.forte.ui.view.ViewActivity

fun SongQueueQuery.Song.notificationContent(): NotificationContent =
    NotificationContent(
        title = name(),
        text = "${album().name()} • ${artists().joinToString(", ") { it.name() }}"
    )

class NotificationMetadataProvider(
    private val queue: Queue,
    private val context: Context,
    private val fetcher: BitmapFetcher,
    private val player: Player,
    private val scope: CoroutineScope
) : MediaDescriptionAdapter {
    // TODO: Only Show Notification When Queue Is Non-Empty
    private val nowPlaying: QueueItem
        get() = queue.getNowPlaying(player)!!

    private val cachedContent = SingleValueCache<String, Deferred<NotificationContent>>()
    override val content: Deferred<NotificationContent>
        get() = cachedContent.computeIfAbsent(nowPlaying.songId) {
            scope.async {
                nowPlaying.song.await().notificationContent()
            }
        }

    private val cachedLargeIcon = SingleValueCache<String, Deferred<Bitmap?>>()
    override val largeIcon: Deferred<Bitmap?>
        get() = cachedLargeIcon.computeIfAbsent(nowPlaying.songId) {
            scope.async {
                val artworkUrl = nowPlaying.song.await().album().artworkUrl() ?: return@async null
                fetcher.getAsync(artworkUrl).await()
            }
        }

    override val contentIntent: PendingIntent
        get() {
            val intent = Intent(context, ViewActivity::class.java).apply {
                putExtras(ViewActivity.Extras.OpenNowPlaying.build())
            }

            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
}