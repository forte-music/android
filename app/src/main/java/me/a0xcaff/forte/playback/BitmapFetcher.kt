package me.a0xcaff.forte.playback

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import kotlinx.coroutines.*
import me.a0xcaff.forte.SingleValueCache
import me.a0xcaff.forte.get

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <T> returnNowOrLater(scope: CoroutineScope, source: Deferred<T>, callback: (T) -> Unit): T? {
    if (source.isCompleted) {
        return source.getCompleted()
    }

    scope.launch {
        callback(source.await())
    }

    return null
}

/**
 * Handles fetching bitmaps. The latest bitmap is cached in a [SingleValueCache]. Used for notification and media
 * session where only one image should be loaded at a time.
 */
class BitmapFetcher(
    private val picasso: Picasso,
    private val transform: RequestCreator.() -> Unit
) {
    private val singleValueCache = SingleValueCache<String, Deferred<Bitmap>>()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun getAsync(url: String): Deferred<Bitmap> =
        singleValueCache.computeIfAbsent(url) {
            coroutineScope.async { picasso.get(url, transform) }
        }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    fun release() {
        coroutineScope.cancel()
    }
}
