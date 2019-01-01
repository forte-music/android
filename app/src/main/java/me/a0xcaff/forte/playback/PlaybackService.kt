package me.a0xcaff.forte.playback

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.util.NotificationUtil
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

    // TODO: Shouldn't Have to Use Two Bitmap Fetchers
    private val notificationBitmapFetcher = BitmapFetcher(picasso)

    private val mediaSessionBitmapFetcher = BitmapFetcher(picasso)

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
            override fun getCurrentContentTitle(player: Player): String = "Hello World (Title!)"

            override fun getCurrentContentText(player: Player): String? = "Hello World (Subtitle!)"

            override fun createCurrentContentIntent(player: Player): PendingIntent {
                val intent = Intent(context, ViewActivity::class.java)
                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? = notificationBitmapFetcher.getFor(
                "https://i.scdn.co/image/d345ab2a8278434f1c8cc936ace70da02ac845fb",
                {
                    resizeDimen(R.dimen.notification_large_artwork_size, R.dimen.notification_large_artwork_size)
                    centerInside()
                },
                callback::onBitmap
            )
        }

        NotificationUtil.createNotificationChannel(
            this,
            NOW_PLAYING_CHANNEL_ID,
            R.string.playback_notification_channel_name,
            NotificationUtil.IMPORTANCE_LOW
        )

        playerNotificationManager = PlayerNotificationManager(
            player,
            this,
            metadataDescriptor,
            mediaSession.sessionToken,
            NOW_PLAYING_NOTIFICATION_ID,
            NOW_PLAYING_CHANNEL_ID
        )

        mediaSessionConnector = MediaSessionConnector(
            mediaSession,
            null,
            MediaSessionConnector.MediaMetadataProvider {
                val builder = MediaMetadataCompat.Builder()

                val bitmap = mediaSessionBitmapFetcher.getFor(
                    "https://i.scdn.co/image/d345ab2a8278434f1c8cc936ace70da02ac845fb",
                    {
                        resizeDimen(R.dimen.media_session_max_artwork_size, R.dimen.media_session_max_artwork_size)
                        centerInside()
                    }, {
                        mediaSessionConnector.invalidateMediaSessionMetadata()
                    }
                )

                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)

                builder.build()
            }
        ).apply {
            setPlayer(player, null)
        }

        binder = PlaybackServiceBinderImpl(player)
    }

    override fun onDestroy() {
        super.onDestroy()
        binder.release()
        mediaSession.release()
        playerNotificationManager.release()
        notificationBitmapFetcher.release()
        mediaSessionBitmapFetcher.release()
        mediaSessionConnector.setPlayer(null, null)
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = Service.START_NOT_STICKY

    // TODO: Handle Error
    // TODO: Handle Queue
    // TODO: Wakelocks
}

/// player
//    .createMessage((type, payload) -> Log.d("POSITION", "message triggered at 5 seconds."))
//    .setPosition(5000)
//    .setDeleteAfterDelivery(false)
//    .setHandler(new Handler())
//    .send();
