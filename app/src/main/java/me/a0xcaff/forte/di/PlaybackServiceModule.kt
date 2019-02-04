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
import org.koin.core.parameter.parametersOf
import org.koin.dsl.context.ModuleDefinition
import org.koin.dsl.definition.Definition
import org.koin.dsl.module.module

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

    playbackScopeService("MediaSession BitmapFetcher") { service ->
        BitmapFetcher(
            picasso = get(),
            scope = service.scope
        ) {
            resizeDimen(R.dimen.media_session_max_artwork_size, R.dimen.media_session_max_artwork_size)
            centerInside()
        }
    }

    playbackScopeService("Notification BitmapFetcher") { service ->
        BitmapFetcher(
            picasso = get(),
            scope = service.scope
        ) {
            resizeDimen(R.dimen.notification_large_artwork_size, R.dimen.notification_large_artwork_size)
            centerInside()
        }
    }

    playbackScopeService { service ->
        Backend(
            apolloClient = get(),
            baseUri = Uri.parse("http://10.0.2.2:3000/"),
            quality = Quality.RAW,
            scope = service.scope
        )
    }

    playbackScope {
        Queue()
    }

    playbackScope {
        val mediaSource = ConcatenatingMediaSource()
        val mediaSourceFactory = ExtractorMediaSource.Factory(get<OkHttpDataSourceFactory>())
        val queue = get<Queue>()

        val updater = ConcatenatingMediaSourceUpdater(
            mediaSource,
            mediaSourceFactory
        )

        queue.registerObserver(updater)

        mediaSource as MediaSource
    }

    playbackScope {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    playbackScopeService { service ->
        ExoPlayerFactory.newSimpleInstance(service).apply {
            prepare(get())

            val audioAttributes = get<AudioAttributes>()
            setAudioAttributes(audioAttributes, true)
        }
    }


    playbackScopeService("Notification Channel") { service ->
        NotificationUtil.createNotificationChannel(
            service,
            NOW_PLAYING_CHANNEL_ID,
            R.string.playback_notification_channel_name,
            NotificationUtil.IMPORTANCE_LOW
        )
    }

    playbackScopeService { service ->
        NotificationMetadataProvider(
            queue = get(),
            context = service,
            fetcher = playbackScopeGet(service, "Notification BitmapFetcher"),
            player = playbackScopeGet<SimpleExoPlayer>(service),
            scope = service.scope
        )
    }

    playbackScopeService { service ->
        MediaSessionCompat(service, "ForteMediaPlaybackService").apply {
            isActive = true
        }
    }

    playbackScopeService { service ->
        MediaSessionMetadataProvider.withConnector(
            mediaSession = playbackScopeGet(service),
            queue = get(),
            fetcher = playbackScopeGet(service, "MediaSession BitmapFetcher")
        ).apply {
            val player = playbackScopeGet<SimpleExoPlayer>(service)
            setPlayer(player, null)
        }
    }

    playbackScopeService { service ->
        playbackScopeGet<Unit>(service, "Notification Channel")

        PlayerNotificationManager(
            player = playbackScopeGet<SimpleExoPlayer>(service),
            service = service,
            mediaDescriptionAdapter = playbackScopeGet<NotificationMetadataProvider>(service),
            mediaSessionCompatToken = playbackScopeGet<MediaSessionCompat>(service).sessionToken,
            notificationId = NOW_PLAYING_NOTIFICATION_ID,
            channelId = NOW_PLAYING_CHANNEL_ID,
            scope = service.scope
        )
    }

    playbackScopeService { service ->
        PlaybackServiceBinderImpl(
            player = playbackScopeGet<SimpleExoPlayer>(service),
            queue = get()
        )
    }
}

inline fun <reified T : Any> ModuleDefinition.playbackScope(name: String = "", noinline definition: Definition<T>) =
    scope(PlaybackService::javaClass.name, name = name, definition = definition)

inline fun <reified T : Any> ModuleDefinition.playbackScopeService(
    name: String = "",
    crossinline builder: (PlaybackService) -> T
) = scope(PlaybackService::javaClass.name, name) { (service: PlaybackService) -> builder(service) }

inline fun <reified T : Any> ModuleDefinition.playbackScopeGet(service: PlaybackService, name: String = ""): T =
    get(name, PlaybackService::javaClass.name) { parametersOf(service) }

fun KoinContext.createPlaybackScope() =
    createScope(PlaybackService::javaClass.name)
