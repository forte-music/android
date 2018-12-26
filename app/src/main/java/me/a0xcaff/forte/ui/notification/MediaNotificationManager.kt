package me.a0xcaff.forte.ui.notification

import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat

const val NOW_PLAYING_NOTIFICATION_ID = 0xcaff

/**
 * Keeps a notification up to date with the provided [MediaSessionCompat].
 */
class MediaNotificationManager(private val service: MediaBrowserServiceCompat, mediaSessionCompat: MediaSessionCompat) {
    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { updateNotification(it) }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaControllerCompat.playbackState?.let { updateNotification(it) }
        }
    }

    private val notificationBuilder = NotificationBuilder(service)

    private val notificationManager = NotificationManagerCompat.from(service)

    private var isForegroundService = false

    private val mediaControllerCompat = MediaControllerCompat(service, mediaSessionCompat)

    init {
        mediaControllerCompat.registerCallback(callback)
    }

    private fun updateNotification(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_PLAYING -> {
                val notification = notificationBuilder.buildNotification(mediaControllerCompat.sessionToken)

                if (!isForegroundService) {
                    service.startService(Intent(service.applicationContext, service.javaClass))
                    service.startForeground(NOW_PLAYING_NOTIFICATION_ID, notification)
                    isForegroundService = true
                } else {
                    notificationManager.notify(NOW_PLAYING_NOTIFICATION_ID, notification)
                }
            }
            else -> {
                if (isForegroundService) {
                    service.stopForeground(false)
                    isForegroundService = false

                    if (state.state == PlaybackStateCompat.STATE_NONE) {
                        service.stopSelf()
                        service.stopForeground(true)
                    } else {
                        val notification = notificationBuilder.buildNotification(mediaControllerCompat.sessionToken)
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION_ID, notification)
                    }
                }
            }
        }
    }
}
