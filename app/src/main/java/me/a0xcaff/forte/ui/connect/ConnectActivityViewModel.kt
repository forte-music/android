/* ktlint-disable no-wildcard-imports */
package me.a0xcaff.forte.ui.connect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import me.a0xcaff.forte.*
import okhttp3.HttpUrl
import kotlin.coroutines.CoroutineContext

class ConnectActivityViewModel(
    private val parentContext: CoroutineContext = Dispatchers.Main,
    private val serverValidator: ServerValidator
) : ViewModel(), CoroutineScope {
    private val scopeJob = Job()

    override val coroutineContext: CoroutineContext
        get() = parentContext + scopeJob

    private val _url = MutableLiveData<String>().default("")

    private val _validationError = MutableLiveData<String?>().default(null)

    private val _canConnect = MutableLiveData<Boolean>().default(false)

    private val _isLoading = MutableLiveData<Boolean>().default(false)

    private val _error = MutableLiveData<String>().default("")

    private var responseJob: Job? = null

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

    fun connect(): Job {
        startConnect()

        responseJob = launch {
            val result = serverValidator.validate(httpUrl!!)

            when (result) {
                is Failure -> {
                    failedConnect(result.message)
                }
                is Success -> {
                    // TODO: Happy Path
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
