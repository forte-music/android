package me.a0xcaff.forte.ui.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import me.a0xcaff.forte.R
import me.a0xcaff.forte.isPlaying

const val NOW_PLAYING_CHANNEL_ID = "me.a0xcaff.forte.ui.notification"

class NotificationBuilder(val context: Context) {
    init {
        val platformNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNowPlayingChannelExists(platformNotificationManager)
    }

    private val playAction = NotificationCompat.Action(
        R.drawable.ic_play,
        "Play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    private val pauseAction = NotificationCompat.Action(
        R.drawable.ic_pause,
        "Pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    private val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)

    fun buildNotification(sessionToken: MediaSessionCompat.Token): Notification {
        val controller = MediaControllerCompat(context, sessionToken)
        val description = controller.metadata.description
        val playbackState = controller.playbackState

        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL_ID)

        val playPauseAction = when (playbackState.isPlaying) {
            true -> pauseAction
            false -> playAction
        }
        builder.addAction(playPauseAction)

        val mediaStyle = MediaStyle()
            .setMediaSession(sessionToken)
            .setShowActionsInCompactView(0)
            .setCancelButtonIntent(stopPendingIntent)
            .setShowCancelButton(true)

        return builder
            .setContentIntent(controller.sessionActivity)
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setDeleteIntent(stopPendingIntent)
            .setLargeIcon(description.iconBitmap)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_music_note)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun ensureNowPlayingChannelExists(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !notificationManager.channelExists(NOW_PLAYING_CHANNEL_ID)) {
            createNowPlayingChannel(notificationManager)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNowPlayingChannel(platformNotificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOW_PLAYING_CHANNEL_ID,
            context.getString(R.string.playback_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )

        channel.description = context.getString(R.string.playback_notification_channel_description)

        platformNotificationManager.createNotificationChannel(channel)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NotificationManager.channelExists(id: String): Boolean = getNotificationChannel(id) != null
