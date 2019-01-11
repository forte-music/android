package me.a0xcaff.forte

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gets and renders the image asynchronously. Allows for cancellations. If the value is in the cache, will be returned
 * without suspension immediately. Processing is done on a background thread decided by picasso.
 *
 * The params are weird to support cancellations.
 */
suspend fun Picasso.get(url: String, transform: RequestCreator.() -> Unit): Bitmap =
    suspendCancellableCoroutine { continuation ->
        val tag = Any()

        load(url)
            .tag(tag)
            .apply(transform)
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    // Do nothing.
                }

                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) =
                    continuation.resumeWithException(e)

                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) = continuation.resume(bitmap)
            })

        continuation.invokeOnCancellation { cancelTag(tag) }
    }
