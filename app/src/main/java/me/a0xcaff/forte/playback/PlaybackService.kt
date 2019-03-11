package me.a0xcaff.forte.playback

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import me.a0xcaff.forte.di.createPlaybackScope
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.core.scope.Scope

/**
 * Service responsible for playing audio and keeping the notification up to date.
 */
class PlaybackService : Service() {
    private lateinit var binder: PlaybackServiceBinderImpl
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var player: SimpleExoPlayer
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var playbackScope: Scope

    val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        playbackScope = getKoin().createPlaybackScope()
        playbackScope.addInstance(this)

        binder = get()
        mediaSession = get()
        playerNotificationManager = get()
        player = get()
        mediaSessionConnector = get()

        val queue = get<Queue>()
        val backend = get<Backend>()

        queue.add(
            QueueItem("00000000000000000000000000000001", backend),
            QueueItem("00000000000000000000000000000002", backend)
        )
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun onDestroy() {
        super.onDestroy()
        binder.release()
        playerNotificationManager.release()
        mediaSession.release()
        mediaSessionConnector.setPlayer(null, null)
        player.release()
        scope.cancel()
        playbackScope.close()
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
