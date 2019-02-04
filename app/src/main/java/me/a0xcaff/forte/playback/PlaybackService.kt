package me.a0xcaff.forte.playback

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.apollographql.apollo.ApolloClient
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.util.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import me.a0xcaff.forte.R
import org.koin.android.ext.android.get

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

    private lateinit var backend: Backend

    private val upstreamDataSourceFactory: OkHttpDataSourceFactory = get()

    private val notificationBitmapFetcher: BitmapFetcher = get("Notification BitmapFetcher")

    private val mediaSessionBitmapFetcher: BitmapFetcher = get("MediaSession BitmapFetcher")

    private val apolloClient: ApolloClient = get()

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        val context = this

        backend = Backend(
            apolloClient,
            Uri.parse("http://10.0.2.2:3000/"),
            Quality.RAW,
            scope
        )

        val queue = Queue()
        val mediaSource = ConcatenatingMediaSource()
        val mediaSourceFactory = ExtractorMediaSource.Factory(upstreamDataSourceFactory)

        val updater = ConcatenatingMediaSourceUpdater(
            mediaSource,
            mediaSourceFactory
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
            notificationBitmapFetcher,
            player,
            scope
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
            NOW_PLAYING_CHANNEL_ID,
            scope
        )

        mediaSessionConnector = MediaSessionMetadataProvider.withConnector(
            mediaSession,
            queue,
            mediaSessionBitmapFetcher
        ).apply {
            setPlayer(player, null)
        }

        binder = PlaybackServiceBinderImpl(player, queue)

        queue.add(
            QueueItem("00000000000000000000000000000001", backend),
            QueueItem("00000000000000000000000000000002", backend)
        )
        player.prepare(mediaSource)
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun onDestroy() {
        super.onDestroy()
        binder.release()
        playerNotificationManager.release()
        mediaSession.release()
        notificationBitmapFetcher.release()
        mediaSessionBitmapFetcher.release()
        mediaSessionConnector.setPlayer(null, null)
        player.release()
        scope.cancel()
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
