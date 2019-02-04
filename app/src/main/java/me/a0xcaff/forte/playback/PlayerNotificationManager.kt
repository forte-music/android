package me.a0xcaff.forte.playback

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import me.a0xcaff.forte.R
import me.a0xcaff.forte.previousOrBeginning

class PlayerNotificationManager(
    private val player: Player,
    service: Service,
    private val mediaDescriptionAdapter: MediaDescriptionAdapter,
    private val mediaSessionCompatToken: MediaSessionCompat.Token,
    notificationId: Int,
    private val channelId: String,
    private val scope: CoroutineScope
) {
    private val context = service

    private val actions = DefaultActions(context, notificationId)
    private val broadcastReceiver = NotificationBroadcastReceiver(notificationId)
    private val notificationDispatcher = NotificationDispatcher(notificationId, service, player)
    private val listener = PlayerListener(player, this::startOrUpdateNotification)

    private var isNotificationStarted = false

    init {
        player.addListener(listener)
    }

    fun release() {
        player.removeListener(listener)
        stopNotification()
    }

    private fun invalidate() {
        if (isNotificationStarted) {
            updateNotification()
        }
    }

    private fun <T> loadDeferred(deferred: Deferred<T>) =
        returnNowOrLater(scope, deferred) { invalidate() }

    private fun updateNotification() {
        val notification = createNotification()
        notificationDispatcher.notify(notification)
    }

    private fun startOrUpdateNotification() {
        updateNotification()
        if (!isNotificationStarted) {
            isNotificationStarted = true
            context.registerReceiver(broadcastReceiver, ACTION_INTENT_FILTER)
        }
    }

    private fun stopNotification() {
        if (isNotificationStarted) {
            notificationDispatcher.cancel()
            isNotificationStarted = false
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    private fun createNotification(): Notification {
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        mediaStyle.setMediaSession(mediaSessionCompatToken)

        val builder = NotificationCompat.Builder(context, channelId)
        builder.setStyle(mediaStyle)

        // Actions
        getActions().forEach { builder.addAction(it) }
        mediaStyle.setShowActionsInCompactView(1)

        // Dismiss Action
        mediaStyle.setShowCancelButton(true)
        mediaStyle.setCancelButtonIntent(actions.cancelPendingIntent)
        builder.setDeleteIntent(actions.cancelPendingIntent)

        builder.apply {
            setOngoing(false)
            setSmallIcon(R.drawable.exo_notification_small_icon)
            priority = NotificationCompat.PRIORITY_LOW

            val content =
                loadDeferred(mediaDescriptionAdapter.content) ?: NotificationContent(title = "...", text = "...")

            setContentTitle(content.title)
            setContentText(content.text)

            val bitmap = loadDeferred(mediaDescriptionAdapter.largeIcon)
            setLargeIcon(bitmap)

            setContentIntent(mediaDescriptionAdapter.contentIntent)
            setShowWhen(false)
        }

        return builder.build()
    }

    private fun getActions(): Sequence<NotificationCompat.Action> = sequence {
        yield(actions.previous)

        yield(
            when (player.playWhenReady) {
                true -> actions.pause
                false -> actions.play
            }
        )

        if (player.nextWindowIndex != C.INDEX_UNSET) {
            yield(actions.next)
        }
    }

    private class PlayerListener(
        private val player: Player,
        private val startOrUpdateNotification: () -> Unit
    ) : Player.EventListener {
        var lastPlayWhenReady = player.playWhenReady
        var lastPlaybackState = player.playbackState

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (
                (lastPlayWhenReady != playWhenReady && playbackState != Player.STATE_IDLE) ||
                lastPlaybackState != playbackState
            ) {
                startOrUpdateNotification()
            }

            lastPlayWhenReady = playWhenReady
            lastPlaybackState = playbackState
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            startOrUpdateIfNotIdle()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            startOrUpdateIfNotIdle()
        }

        override fun onPositionDiscontinuity(reason: Int) {
            startOrUpdateNotification()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            startOrUpdateIfNotIdle()
        }

        private fun startOrUpdateIfNotIdle() {
            if (player.playbackState == Player.STATE_IDLE) {
                return
            }

            startOrUpdateNotification()
        }
    }

    inner class NotificationBroadcastReceiver(private val instanceId: Int) : BroadcastReceiver() {
        private fun isValidMessage(intent: Intent): Boolean {
            if (!isNotificationStarted) {
                return false
            }

            val gotInstanceId = intent.getIntExtra(EXTRA_INSTANCE_ID, -1)
            if (instanceId != gotInstanceId) {
                return false
            }

            return true
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (!isValidMessage(intent)) {
                return
            }

            when (intent.action) {
                ACTION_PLAY -> player.playWhenReady = true
                ACTION_PAUSE -> player.playWhenReady = false
                ACTION_NEXT -> player.next()
                ACTION_PREVIOUS -> player.previousOrBeginning()
                ACTION_CANCEL -> stopNotification()
            }
        }
    }
}

const val EXTRA_INSTANCE_ID = "INSTANCE_ID"

const val ACTION_PREVIOUS = "me.a0xcaff.forte.exoplayer.prev"
const val ACTION_PLAY = "me.a0xcaff.forte.exoplayer.play"
const val ACTION_PAUSE = "me.a0xcaff.forte.exoplayer.pause"
const val ACTION_NEXT = "me.a0xcaff.forte.exoplayer.next"
const val ACTION_CANCEL = "me.a0xcaff.forte.exoplayer.cancel"
val ACTION_INTENT_FILTER = IntentFilter().apply {
    addAction(ACTION_PREVIOUS)
    addAction(ACTION_PLAY)
    addAction(ACTION_PAUSE)
    addAction(ACTION_NEXT)
    addAction(ACTION_CANCEL)
}

class DefaultActions(private val context: Context, private val instanceId: Int) {
    val previous = NotificationCompat.Action(
        R.drawable.exo_notification_previous,
        context.getString(R.string.exo_controls_previous_description),
        createIntent(ACTION_PREVIOUS)
    )

    val play = NotificationCompat.Action(
        R.drawable.exo_notification_play,
        context.getString(R.string.exo_controls_play_description),
        createIntent(ACTION_PLAY)
    )

    val pause = NotificationCompat.Action(
        R.drawable.exo_notification_pause,
        context.getString(R.string.exo_controls_pause_description),
        createIntent(ACTION_PAUSE)
    )

    val next = NotificationCompat.Action(
        R.drawable.exo_notification_next,
        context.getString(R.string.exo_controls_next_description),
        createIntent(ACTION_NEXT)
    )

    val cancelPendingIntent = createIntent(ACTION_CANCEL)

    private fun createIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_INSTANCE_ID, instanceId)
        }

        return PendingIntent.getBroadcast(context, instanceId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}

class NotificationDispatcher(
    private val notificationId: Int,
    private val service: Service,
    private val player: Player
) {
    private val notificationManager = NotificationManagerCompat.from(service)

    private var foregrounded = false

    fun notify(notification: Notification) {
        if (player.playWhenReady && !foregrounded) {
            Util.startForegroundService(
                service,
                Intent(service, PlaybackService::class.java)
            )

            service.startForeground(notificationId, notification)
            foregrounded = true
        } else {
            notificationManager.notify(notificationId, notification)
        }

        if (!player.playWhenReady && foregrounded) {
            service.stopForeground(false)
            foregrounded = false
        }
    }

    fun cancel() {
        notificationManager.cancel(notificationId)
        service.stopForeground(true)
        service.stopSelf()
        foregrounded = false
    }
}
