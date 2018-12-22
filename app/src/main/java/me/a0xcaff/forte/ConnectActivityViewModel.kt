package me.a0xcaff.forte

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.a0xcaff.forte.graphql.TestQuery
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class ConnectActivityViewModel : ViewModel() {
    private val _url = MutableLiveData<String>().default("")

    private val _validationError = MutableLiveData<String?>().default(null)

    private val _canConnect = MutableLiveData<Boolean>().default(false)

    private val _isLoading = MutableLiveData<Boolean>().default(false)

    private val _error = MutableLiveData<String>().default("")

    private var connectingJob: Job? = null

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

    fun connect() {
        startConnect()

        val okHttpClient = OkHttpClient.Builder().build()

        val apolloClient = ApolloClient.builder()
            .serverUrl(httpUrl!!)
            .okHttpClient(okHttpClient)
            .build()

        val query = TestQuery.builder().build()

        connectingJob = GlobalScope.launch {
            try {
                val response = apolloClient.query(query).executeAsync()
                launch(Dispatchers.Main) {
                    finishConnect()

                    if (response.hasErrors()) {
                        val message = response.errors().joinToString(separator = "\n", transform = { it.message() ?: "" })
                        failedConnect(message)
                    }
                }

                // TODO: Happy Path
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    finishConnect()
                    failedConnect(e.message ?: "")
                }
            }
        }
    }

    fun cancelConnecting() {
        connectingJob?.cancel()
        connectingJob = null
        finishConnect()
    }

    override fun onCleared() {
        cancelConnecting()
    }
}
