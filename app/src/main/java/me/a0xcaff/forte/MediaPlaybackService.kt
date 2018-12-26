package me.a0xcaff.forte

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import me.a0xcaff.forte.ui.notification.MediaNotificationManager

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var notificationManager: MediaNotificationManager

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "ForteMediaPlaybackService")
        notificationManager = MediaNotificationManager(this, mediaSession)

        sessionToken = mediaSession.sessionToken
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? =
        BrowserRoot(
            getString(R.string.app_name),
            null
        )

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }
}
