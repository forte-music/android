package me.a0xcaff.forte

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectActivityViewModel : ViewModel() {
    private val _url = MutableLiveData<String>().default("")

    private val _error = MutableLiveData<String?>().default(null)

    private val _canConnect = MutableLiveData<Boolean>().default(false)

    private val _isLoading = MutableLiveData<Boolean>().default(false)

    val url: LiveData<String> get() = _url

    val error: LiveData<String?> get() = _error

    val canConnect: LiveData<Boolean> get() = _canConnect

    val isLoading: LiveData<Boolean> get() = _isLoading

    fun urlChanged(newValue: String) {
        _url.value = newValue
        _error.value = if (!newValue.isEmpty() && newValue.length < 3) {
            "Add more chars!"
        } else {
            null
        }

        _canConnect.value = newValue.length >= 3
    }

    fun connect() {
        _isLoading.value = true
        _canConnect.value = false
    }
}
