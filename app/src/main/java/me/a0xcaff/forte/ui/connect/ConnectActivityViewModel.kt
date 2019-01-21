/* ktlint-disable no-wildcard-imports */
package me.a0xcaff.forte.ui.connect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.a0xcaff.forte.Config
import me.a0xcaff.forte.Failure
import me.a0xcaff.forte.ServerValidator
import me.a0xcaff.forte.Success
import me.a0xcaff.forte.ui.Event
import me.a0xcaff.forte.default
import okhttp3.HttpUrl
import kotlin.coroutines.CoroutineContext

class ConnectActivityViewModel(
    private val parentContext: CoroutineContext = Dispatchers.Main,
    private val serverValidator: ServerValidator,
    private val config: Config
) : ViewModel(), CoroutineScope {
    private val scopeJob = Job()

    override val coroutineContext: CoroutineContext
        get() = parentContext + scopeJob

    private val _url = MutableLiveData<String>().default("")
    val url: LiveData<String> = _url

    private val _validationError = MutableLiveData<String?>().default(null)
    val validationError: LiveData<String?> = _validationError

    private val _canConnect = MutableLiveData<Boolean>().default(false)
    val canConnect: LiveData<Boolean> = _canConnect

    private val _isLoading = MutableLiveData<Boolean>().default(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>().default("")
    val error: LiveData<String> = _error

    private val _successfulUrlFound = MutableLiveData<Event<Unit>>()
    val sucessfulUrlFound: LiveData<Event<Unit>> = _successfulUrlFound

    private var responseJob: Job? = null

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

    fun connect(): Job {
        startConnect()

        responseJob = launch {
            val result = serverValidator.validate(httpUrl!!)

            when (result) {
                is Failure -> {
                    failedConnect(result.message)
                }
                is Success -> {
                    config.serverUrl = httpUrl
                    _successfulUrlFound.value = Event(Unit)
                }
            }
            finishConnect()
        }

        return responseJob!!
    }

    fun cancelConnecting() {
        responseJob?.cancel()
        responseJob = null
        finishConnect()
    }

    override fun onCleared() {
        cancelConnecting()
        scopeJob.cancel()
    }
}
