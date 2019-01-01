package me.a0xcaff.forte.playback

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import com.squareup.picasso.Picasso
import me.a0xcaff.forte.R
import me.a0xcaff.forte.ui.view.ViewActivity
import org.koin.android.ext.android.inject

const val NOW_PLAYING_NOTIFICATION_ID = 0xcaff
const val NOW_PLAYING_CHANNEL_ID = "me.a0xcaff.forte.ui.notification"

/**
 * Service responsible for playing audio and keeping the notification up to date.
 */
class PlaybackService : Service() {
    private lateinit var binder: PlaybackServiceBinderImpl

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var playerNotificationManager: PlayerNotificationManager

    private lateinit var player: SimpleExoPlayer

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val upstreamDataSourceFactory: OkHttpDataSourceFactory by inject()

    private val picasso: Picasso by inject()

    private val bitmapFetcher = BitmapFetcher(picasso)

    override fun onCreate() {
        super.onCreate()
        val context = this

        mediaSession = MediaSessionCompat(this, "ForteMediaPlaybackService")
        mediaSession.isActive = true

        player = ExoPlayerFactory.newSimpleInstance(this).apply {
            val mediaSourceFactory = ExtractorMediaSource.Factory(upstreamDataSourceFactory)
            val mediaSource =
                mediaSourceFactory.createMediaSource(Uri.parse("http://192.168.1.160:3000/files/music/00000000000000000000000000000001/raw"))

            prepare(mediaSource)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()

            setAudioAttributes(audioAttributes, true)
        }

        val metadataDescriptor = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                val intent = Intent(context, ViewActivity::class.java)
                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            override fun getCurrentContentText(player: Player?): String? = "Hello World (Subtitle!)"

            override fun getCurrentContentTitle(player: Player?): String = "Hello World (Title!)"

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? = bitmapFetcher.getFor(
                "https://i.scdn.co/image/d345ab2a8278434f1c8cc936ace70da02ac845fb",
                {
                    resizeDimen(R.dimen.notification_large_artwork_size, R.dimen.notification_large_artwork_size)
                    centerInside()
                },
                { callback.onBitmap(it) }
            )
        }

        val notificationDispatcher = object : PlayerNotificationManager.NotificationDispatcher {
            var foregrounded = false
            val notificationManager = NotificationManagerCompat.from(context)

            override fun notify(notificationId: Int, notification: Notification) {
                if (player.playWhenReady && !foregrounded) {
                    Util.startForegroundService(
                        context,
                        Intent(context, PlaybackService::class.java)
                    )

                    startForeground(notificationId, notification)
                    foregrounded = true
                } else {
                    notificationManager.notify(notificationId, notification)
                }

                if (!player.playWhenReady && foregrounded) {
                    stopForeground(false)
                    foregrounded = false
                }
            }

            override fun cancel(notificationId: Int) {
                notificationManager.cancel(notificationId)
                stopForeground(true)
                // stopSelf()
                foregrounded = false
            }
        }

        NotificationUtil.createNotificationChannel(
            this,
            NOW_PLAYING_CHANNEL_ID,
            R.string.playback_notification_channel_name,
            NotificationUtil.IMPORTANCE_LOW
        )

        playerNotificationManager = PlayerNotificationManager(
            this,
            NOW_PLAYING_CHANNEL_ID,
            NOW_PLAYING_NOTIFICATION_ID,
            metadataDescriptor,
            null,
            notificationDispatcher
        ).apply {
            setMediaSessionToken(mediaSession.sessionToken)
            setFastForwardIncrementMs(0)
            setRewindIncrementMs(0)
            // TODO: It looks like cancel is only called if the following line is commented.
            // setStopAction(null)
            setOngoing(false)

            setPlayer(player)
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession, null, MediaSessionConnector.MediaMetadataProvider {
            val builder = MediaMetadataCompat.Builder()

            val bitmap = bitmapFetcher.getFor(
                "https://i.scdn.co/image/d345ab2a8278434f1c8cc936ace70da02ac845fb",
                {
                    resizeDimen(R.dimen.media_session_max_artwork_size, R.dimen.media_session_max_artwork_size)
                    centerInside()
                }, { mediaSessionConnector.invalidateMediaSessionMetadata() }
            )

            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)

            builder.build()
        }).apply {
            setPlayer(player, null)
        }

        binder = PlaybackServiceBinderImpl(player)
    }

    override fun onDestroy() {
        super.onDestroy()
        binder.release()
        mediaSession.release()
        playerNotificationManager.setPlayer(null)
        bitmapFetcher.release()
        mediaSessionConnector.setPlayer(null, null)
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    // TODO: Handle Error
    // TODO: Handle Queue
    // TODO: Wakelocks
    // TODO: Service Isn't Stopped Properly
}

/// player
//    .createMessage((type, payload) -> Log.d("POSITION", "message triggered at 5 seconds."))
//    .setPosition(5000)
//    .setDeleteAfterDelivery(false)
//    .setHandler(new Handler())
//    .send();
