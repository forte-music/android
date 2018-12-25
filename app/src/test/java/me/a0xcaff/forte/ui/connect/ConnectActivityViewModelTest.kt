/* ktlint-disable no-wildcard-imports */
package me.a0xcaff.forte.ui.connect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.a0xcaff.forte.Failure
import me.a0xcaff.forte.ServerValidator
import me.a0xcaff.forte.Success
import me.a0xcaff.forte.ValidationResult
import okhttp3.HttpUrl
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule

class TestServerValidator(val fn: suspend () -> ValidationResult) : ServerValidator {
    override suspend fun validate(url: HttpUrl) = fn.invoke()
}

class ConnectActivityViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `initial state`() {
        val viewModel = ConnectActivityViewModel(serverValidator = TestServerValidator { Success })

        with(viewModel) {
            assertEquals(url.value, "")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, false)
            assertEquals(error.value, "")
            assertEquals(validationError.value, null)
        }
    }

    @Test
    fun `invalid url`() {
        val viewModel = ConnectActivityViewModel(serverValidator = TestServerValidator { Success })
        viewModel.urlChanged("this is an invalid url")

        with(viewModel) {
            assertEquals(url.value, "this is an invalid url")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, false)
            assertEquals(error.value, "")
            assertEquals(validationError.value, "Invalid URL")
        }
    }

    @Test
    fun `valid url without service`() {
        val viewModel = ConnectActivityViewModel(
            GlobalScope.coroutineContext,
            serverValidator = TestServerValidator { Failure("this is an invalid endpoint") })
        viewModel.urlChanged("http://example.com")
        val job = viewModel.connect()

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, true)
            assertEquals(canConnect.value, false)
            assertEquals(validationError.value, null)
        }

        runBlocking {
            job.join()
        }

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, true)
            assertEquals(error.value, "this is an invalid endpoint")
            assertEquals(validationError.value, null)
        }
    }

    @Test
    fun `cancel invalid request`() {
        val viewModel = ConnectActivityViewModel(GlobalScope.coroutineContext, serverValidator = TestServerValidator {
            delay(1)
            Failure("something went wrong, but we cancelled first")
        })
        viewModel.urlChanged("http://example.com")
        viewModel.connect()

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, true)
            assertEquals(canConnect.value, false)
            assertEquals(validationError.value, null)
        }

        viewModel.cancelConnecting()

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, true)
            assertEquals(error.value, "")
            assertEquals(validationError.value, null)
        }
    }

    @Test
    fun `valid request`() {
        val viewModel =
            ConnectActivityViewModel(GlobalScope.coroutineContext, serverValidator = TestServerValidator { Success })

        viewModel.urlChanged("http://example.com")
        val job = viewModel.connect()

        runBlocking {
            job.join()
        }

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, true)
            assertEquals(error.value, "")
            assertEquals(validationError.value, null)
        }
    }
}
