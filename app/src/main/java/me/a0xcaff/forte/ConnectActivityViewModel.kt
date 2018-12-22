package me.a0xcaff.forte

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import me.a0xcaff.forte.graphql.TestQuery
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class ConnectActivityViewModel : ViewModel() {
    private val _url = MutableLiveData<String>().default("")

    private val _error = MutableLiveData<String?>().default(null)

    private val _canConnect = MutableLiveData<Boolean>().default(false)

    private val _isLoading = MutableLiveData<Boolean>().default(false)

    val url: LiveData<String> get() = _url

    val error: LiveData<String?> get() = _error

    val canConnect: LiveData<Boolean> get() = _canConnect

    val isLoading: LiveData<Boolean> get() = _isLoading

    private val httpUrl get() = HttpUrl.parse(url.value!!)

    fun urlChanged(newValue: String) {
        _url.value = newValue

        _error.value = if (!url.value!!.isEmpty() && httpUrl == null) {
            "Invalid URL"
        } else {
            null
        }

        _canConnect.value = !url.value!!.isEmpty() && httpUrl != null
    }

    fun connect() {
        _isLoading.value = true
        _canConnect.value = false

        val okHttpClient = OkHttpClient.Builder().build()

        val apolloClient = ApolloClient.builder()
            .serverUrl(httpUrl!!)
            .okHttpClient(okHttpClient)
            .build()

        val query = TestQuery.builder().build()

        apolloClient.query(query).enqueue(object : ApolloCall.Callback<TestQuery.Data>() {
            override fun onFailure(e: ApolloException) {
                println(e)
            }

            override fun onResponse(response: Response<TestQuery.Data>) {
                println(response.data().toString())
            }
        })
    }
}
