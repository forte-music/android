package me.a0xcaff.forte.playback

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class BitmapFetcher(private val picasso: Picasso) {
    private var lastTarget: Target? = null

    fun getFor(url: String, transform: RequestCreator.() -> Unit, callback: (Bitmap) -> Unit): Bitmap? {
        val lastTarget = this.lastTarget
        if (lastTarget != null && lastTarget.key == url) {
            return lastTarget.startUsingCallback(callback)
        }

        lastTarget?.cancel()

        val tag = Any()
        val target = Target(picasso, tag, url)

        picasso.load(url)
            .tag(tag)
            .apply(transform)
            .into(target)

        this.lastTarget = target
        return target.startUsingCallback(callback)
    }

    class Target(private val picasso: Picasso, private val tag: Any, val key: String) : com.squareup.picasso.Target {
        private val lock = ReentrantLock()
        private var callback: ((Bitmap) -> Unit)? = null
        private var loadedBitmap: Bitmap? = null

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            if (callback == null) {
                throw e
            }

            // Throwing in other case will just end up throwing on the worker thread.
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            lock.withLock {
                val cb = callback
                loadedBitmap = bitmap

                if (cb != null) {
                    cb(bitmap)
                }
            }
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            // Do nothing.
        }

        fun cancel() {
            picasso.cancelTag(tag)
        }

        fun startUsingCallback(cb: (Bitmap) -> Unit): Bitmap? {
            lock.withLock {
                callback = cb
                return loadedBitmap
            }
        }
    }
}
