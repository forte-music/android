package me.a0xcaff.forte.playback

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
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
class MediaPlaybackService : Service() {
    private lateinit var binder: Binder

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var playerNotificationManager: PlayerNotificationManager

    private lateinit var player: SimpleExoPlayer

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val upstreamDataSourceFactory: OkHttpDataSourceFactory by inject()

    private val picasso: Picasso by inject()

    private var notification: Notification? = null

    private val bitmapFetcher = BitmapFetcher(picasso)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .build()

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "ForteMediaPlaybackService")
        mediaSession.isActive = true

        player = ExoPlayerFactory.newSimpleInstance(this).apply {
            val mediaSourceFactory = ExtractorMediaSource.Factory(upstreamDataSourceFactory)
            val mediaSource =
                mediaSourceFactory.createMediaSource(Uri.parse("http://192.168.1.160:3000/files/music/00000000000000000000000000000001/raw"))

            prepare(mediaSource)
            addListener(object : Player.EventListener {
                var isForeground = true

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    val isPaused = playbackState == Player.STATE_READY && !playWhenReady
                    val isEnded = playbackState == Player.STATE_ENDED

                    when {
                        isForeground && (isPaused || isEnded) -> {
                            stopForeground(false)
                            isForeground = false
                        }
                        !isForeground && notification != null -> {
                            Util.startForegroundService(
                                this@MediaPlaybackService,
                                Intent(applicationContext, MediaPlaybackService::class.java)
                            )
                            startForeground(NOW_PLAYING_NOTIFICATION_ID, notification)
                            isForeground = true
                        }
                    }
                }
            })

            setAudioAttributes(this@MediaPlaybackService.audioAttributes, true)
        }

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            NOW_PLAYING_CHANNEL_ID,
            R.string.playback_notification_channel_name,
            NOW_PLAYING_NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                    val intent = Intent(this@MediaPlaybackService, ViewActivity::class.java)
                    return PendingIntent.getActivity(
                        this@MediaPlaybackService,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
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
        ).apply {
            setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int) {
                    stopSelf()
                }

                override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                    this@MediaPlaybackService.notification = notification
                    startForeground(notificationId, notification)
                }
            })

            setMediaSessionToken(mediaSession.sessionToken)
            setFastForwardIncrementMs(0)
            setRewindIncrementMs(0)
            setStopAction(null)
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

        binder = Binder(mediaSession)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        playerNotificationManager.setPlayer(null)
        bitmapFetcher.release()
        mediaSessionConnector.setPlayer(null, null)
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    // TODO: Handle Error
    // TODO: Handle Queue
    // TODO: Wakelocks
    // TODO: Service Isn't Stopped Properly

    /**
     * Interface of [MediaPlaybackService] exposed to the rest of the application.
     */
    class Binder(val mediaSession: MediaSessionCompat) : android.os.Binder() {
        /**
         * Provides access to the current playback state and provides controls for updating the playback state.
         */
        val mediaController: MediaControllerCompat
            get() = mediaSession.controller
    }
}

/// player
//    .createMessage((type, payload) -> Log.d("POSITION", "message triggered at 5 seconds."))
//    .setPosition(5000)
//    .setDeleteAfterDelivery(false)
//    .setHandler(new Handler())
//    .send();
