/* ktlint-disable no-wildcard-imports */
package me.a0xcaff.forte.ui.connect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.firstOrNull
import me.a0xcaff.forte.default
import me.a0xcaff.forte.executeAsync
import me.a0xcaff.forte.graphql.TestQuery
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import kotlin.coroutines.CoroutineContext

class ConnectActivityViewModel(
    private val parentContext: CoroutineContext = Dispatchers.Main
) : ViewModel(), CoroutineScope {
    private val scopeJob = Job()

    override val coroutineContext: CoroutineContext
        get() = parentContext + scopeJob

    private val _url = MutableLiveData<String>().default("")

    private val _validationError = MutableLiveData<String?>().default(null)

    private val _canConnect = MutableLiveData<Boolean>().default(false)

    private val _isLoading = MutableLiveData<Boolean>().default(false)

    private val _error = MutableLiveData<String>().default("")

    private var responseChannel: ReceiveChannel<Any>? = null

    val url: LiveData<String> get() = _url

    val validationError: LiveData<String?> get() = _validationError

    val canConnect: LiveData<Boolean> get() = _canConnect

    val isLoading: LiveData<Boolean> get() = _isLoading

    val error: LiveData<String> get() = _error

    private val httpUrl get() = HttpUrl.parse(url.value!!)

    fun urlChanged(newValue: String) {
        _error.value = ""
        _url.value = newValue

        val isEmpty = newValue.isEmpty()

        _validationError.value = if (!isEmpty && httpUrl == null) {
            "Invalid URL"
        } else {
            null
        }

        _canConnect.value = !isEmpty && httpUrl != null
    }

    private fun startConnect() {
        _error.value = ""
        _isLoading.value = true
        _canConnect.value = false
    }

    private fun finishConnect() {
        _isLoading.value = false
        _canConnect.value = true
    }

    private fun failedConnect(error: String) {
        _error.value = error
    }

    @ObsoleteCoroutinesApi
    fun connect(): Job {
        startConnect()

        val okHttpClient = OkHttpClient.Builder().build()

        val apolloClient = ApolloClient.builder()
            .serverUrl(httpUrl!!)
            .okHttpClient(okHttpClient)
            .build()

        val query = TestQuery.builder().build()
        val respChan = apolloClient.query(query).executeAsync()
        responseChannel = respChan

        return launch {
            try {
                val resp = respChan.firstOrNull() ?: return@launch

                if (resp.hasErrors()) {
                    val message =
                        resp.errors().joinToString(separator = "\n", transform = { it.message() ?: "" })
                    failedConnect(message)
                }

                // TODO: Happy Path
            } catch (e: Exception) {
                failedConnect(e.message ?: "")
            } finally {
                finishConnect()
                respChan.cancel()
            }
        }
    }

    fun cancelConnecting() {
        responseChannel?.cancel()
        responseChannel = null
        finishConnect()
    }

    override fun onCleared() {
        cancelConnecting()
        scopeJob.cancel()
    }
}
