package me.a0xcaff.forte.playback

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.util.NotificationUtil
import com.squareup.picasso.Picasso
import me.a0xcaff.forte.R
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

        val queue = Queue()
        val mediaSource = ConcatenatingMediaSource()
        val mediaSourceFactory = ExtractorMediaSource.Factory(upstreamDataSourceFactory)

        val updater = ConcatenatingMediaSourceUpdater(
            mediaSource,
            mediaSourceFactory,
            Quality.RAW,
            Uri.parse("http://10.0.2.2:3000/")
        )

        queue.registerObserver(updater)

        mediaSession = MediaSessionCompat(this, "ForteMediaPlaybackService")
        mediaSession.isActive = true

        player = ExoPlayerFactory.newSimpleInstance(this).apply {
            prepare(mediaSource)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()

            setAudioAttributes(audioAttributes, true)
        }

        val mediaDescriptionAdapter = NotificationMetadataProvider(
            queue,
            context,
            bitmapFetcher
        )

        NotificationUtil.createNotificationChannel(
            this,
            NOW_PLAYING_CHANNEL_ID,
            R.string.playback_notification_channel_name,
            NotificationUtil.IMPORTANCE_LOW
        )

        playerNotificationManager = PlayerNotificationManager(
            player,
            this,
            mediaDescriptionAdapter,
            mediaSession.sessionToken,
            NOW_PLAYING_NOTIFICATION_ID,
            NOW_PLAYING_CHANNEL_ID
        )

        mediaSessionConnector = MediaSessionMetadataProvider.withConnector(
            mediaSession,
            queue,
            bitmapFetcher
        ).apply {
            setPlayer(player, null)
        }

        binder = PlaybackServiceBinderImpl(player, queue)

        queue.add(
            QueueItem(
                "00000000000000000000000000000001",
                "Stole the Show",
                arrayOf(Artist("Kygo")),
                Album(
                    "00000000000000000000000000000001",
                    "Stole the Show",
                    "https://i.scdn.co/image/d345ab2a8278434f1c8cc936ace70da02ac845fb"
                )
            ),
            QueueItem(
                "00000000000000000000000000000002",
                "I'm That (Remix)",
                arrayOf(Artist("R. City"), Artist("Beenie Man"), Artist("Azealia Banks")),
                Album(
                    "00000000000000000000000000000002",
                    "I'm That (Remix)",
                    "http://is4.mzstatic.com/image/thumb/Music5/v4/08/da/96/08da9619-3f9b-7c95-60d1-6c18cfdd4dbd/source/600x600bb.jpg"
                )
            )
        )
        player.prepare(mediaSource)
    }

    override fun onDestroy() {
        super.onDestroy()
        binder.release()
        playerNotificationManager.release()
        mediaSession.release()
        bitmapFetcher.release()
        mediaSessionConnector.setPlayer(null, null)
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = Service.START_STICKY

    // TODO: Handle Error
    // TODO: Wakelocks
}

/// player
//    .createMessage((type, payload) -> Log.d("POSITION", "message triggered at 5 seconds."))
//    .setPosition(5000)
//    .setDeleteAfterDelivery(false)
//    .setHandler(new Handler())
//    .send();
