package me.a0xcaff.forte

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.sendBlocking

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <T> ApolloCall<T>.executeAsync(): ReceiveChannel<Response<T>> {
    val channel = Channel<Response<T>>()
    enqueue(object : ApolloCall.Callback<T>() {
        override fun onFailure(e: ApolloException) {
            channel.close(e)
        }

        override fun onResponse(response: Response<T>) {
            channel.sendBlocking(response)
        }
    })

    channel.invokeOnClose {
        this.cancel()
    }

    return channel
}
