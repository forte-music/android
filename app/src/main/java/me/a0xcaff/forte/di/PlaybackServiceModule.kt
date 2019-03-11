package me.a0xcaff.forte.di

import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.util.NotificationUtil
import me.a0xcaff.forte.R
import me.a0xcaff.forte.playback.*
import okhttp3.OkHttpClient
import org.koin.core.KoinContext
import org.koin.dsl.context.ModuleDefinition
import org.koin.dsl.definition.Definition
import org.koin.dsl.module.module

const val NOW_PLAYING_NOTIFICATION_ID = 0xcaff
const val NOW_PLAYING_CHANNEL_ID = "me.a0xcaff.forte.ui.notification"

/**
 * Collection of stuff owned by [PlaybackService].
 */
val PlaybackServiceModule = module {
    playbackScope {
        OkHttpDataSourceFactory(
            get<OkHttpClient>(),
            "Forte Music Android"
        )
    }

    playbackScope("MediaSession BitmapFetcher") {
        BitmapFetcher(
            picasso = get(),
            scope = get<PlaybackService>().scope
        ) {
            resizeDimen(R.dimen.media_session_max_artwork_size, R.dimen.media_session_max_artwork_size)
            centerInside()
        }
    }

    playbackScope("Notification BitmapFetcher") {
        BitmapFetcher(
            picasso = get(),
            scope = get<PlaybackService>().scope
        ) {
            resizeDimen(R.dimen.notification_large_artwork_size, R.dimen.notification_large_artwork_size)
            centerInside()
        }
    }

    playbackScope {
        Backend(
            apolloClient = get(),
            baseUri = Uri.parse("http://10.0.2.2:3000/"),
            quality = Quality.RAW,
            scope = get<PlaybackService>().scope
        )
    }

    playbackScope {
        Queue()
    }

    playbackScope {
        val mediaSource = ConcatenatingMediaSource()
        val mediaSourceFactory = ExtractorMediaSource.Factory(get<OkHttpDataSourceFactory>())
        val queue = get<Queue>()
        val player = get<SimpleExoPlayer>()

        ConcatenatingMediaSourceUpdater(
            mediaSource,
            mediaSourceFactory
        ).let { updater -> queue.registerObserver(updater) }

        PlaybackPreparer(
            queue,
            player,
            mediaSource
        ).let { updater -> queue.registerObserver(updater) }

        mediaSource as MediaSource
    }

    playbackScope {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    playbackScope {
        ExoPlayerFactory.newSimpleInstance(get<PlaybackService>()).apply {
            val audioAttributes = get<AudioAttributes>()
            setAudioAttributes(audioAttributes, true)
        }
    }


    playbackScope("Notification Channel") {
        NotificationUtil.createNotificationChannel(
            get<PlaybackService>(),
            NOW_PLAYING_CHANNEL_ID,
            R.string.playback_notification_channel_name,
            NotificationUtil.IMPORTANCE_LOW
        )
    }

    playbackScope {
        NotificationMetadataProvider(
            queue = get(),
            context = get<PlaybackService>(),
            fetcher = get("Notification BitmapFetcher"),
            player = get<SimpleExoPlayer>(),
            scope = get<PlaybackService>().scope
        )
    }

    playbackScope {
        MediaSessionCompat(get<PlaybackService>(), "ForteMediaPlaybackService").apply {
            isActive = true
        }
    }

    playbackScope {
        MediaSessionMetadataProvider.withConnector(
            mediaSession = get(),
            queue = get(),
            fetcher = get("MediaSession BitmapFetcher")
        ).apply {
            val player = get<SimpleExoPlayer>()
            setPlayer(player, null)
        }
    }

    playbackScope {
        get<Unit>("Notification Channel")

        PlayerNotificationManager(
            player = get<SimpleExoPlayer>(),
            service = get<PlaybackService>(),
            mediaDescriptionAdapter = get<NotificationMetadataProvider>(),
            mediaSessionCompatToken = get<MediaSessionCompat>().sessionToken,
            notificationId = NOW_PLAYING_NOTIFICATION_ID,
            channelId = NOW_PLAYING_CHANNEL_ID,
            scope = get<PlaybackService>().scope
        )
    }

    playbackScope {
        PlaybackServiceBinderImpl(
            player = get<SimpleExoPlayer>(),
            queue = get()
        )
    }
}

inline fun <reified T : Any> ModuleDefinition.playbackScope(name: String = "", noinline definition: Definition<T>) =
    scope(PlaybackService::javaClass.name, name = name, definition = definition)

fun KoinContext.createPlaybackScope() =
    createScope(PlaybackService::javaClass.name)
