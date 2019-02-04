package me.a0xcaff.forte.playback

import android.app.PendingIntent
import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

/**
 * Exposes information about the currently playing media. Values are requested multiple times (even when they don't
 * change). Implementor is responsible for caching.
 */
interface MediaDescriptionAdapter {
    val content: Deferred<NotificationContent>
    val largeIcon: Deferred<Bitmap?>
    val contentIntent: PendingIntent
}

data class NotificationContent(
    val title: String,
    val text: String
)
