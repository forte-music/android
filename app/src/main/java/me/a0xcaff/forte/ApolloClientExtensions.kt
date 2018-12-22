package me.a0xcaff.forte

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> ApolloCall<T>.executeAsync(): Response<T> = suspendCancellableCoroutine { continuation ->
    enqueue(object : ApolloCall.Callback<T>() {
        override fun onFailure(e: ApolloException) {
            // Don't bother with resuming the continuation if it is already cancelled.
            if (continuation.isCancelled) return
            continuation.resumeWithException(e)
        }

        override fun onResponse(response: Response<T>) {
            continuation.resume(response)
        }
    })

    continuation.invokeOnCancellation {
        if (continuation.isCancelled) {
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
    }
}
