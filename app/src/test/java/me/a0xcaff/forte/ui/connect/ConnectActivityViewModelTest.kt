/* ktlint-disable no-wildcard-imports */
package me.a0xcaff.forte.ui.connect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.a0xcaff.forte.*
import okhttp3.HttpUrl
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TestServerValidator(val fn: suspend () -> ValidationResult) : ServerValidator {
    override suspend fun validate(url: HttpUrl) = fn.invoke()
}

class TestConfig : Config {
    override var serverUrl: HttpUrl? = null
}

class ConnectActivityViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `initial state`() {
        val config = TestConfig()
        val viewModel = ConnectActivityViewModel(
            serverValidator = TestServerValidator { Success },
            config = config
        )

        with(viewModel) {
            assertEquals(url.value, "")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, false)
            assertEquals(error.value, "")
            assertEquals(validationError.value, null)
            assertEquals(sucessfulUrlFound.value, null)
        }
        assertNull(config.serverUrl)
    }

    @Test
    fun `invalid url`() {
        val config = TestConfig()
        val viewModel = ConnectActivityViewModel(
            serverValidator = TestServerValidator { Success },
            config = config
        )
        viewModel.urlChanged("this is an invalid url")

        with(viewModel) {
            assertEquals(url.value, "this is an invalid url")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, false)
            assertEquals(error.value, "")
            assertEquals(validationError.value, "Invalid URL")
            assertEquals(sucessfulUrlFound.value, null)
        }
        assertNull(config.serverUrl)
    }

    @Test
    fun `valid url without service`() {
        val config = TestConfig()
        val viewModel = ConnectActivityViewModel(
            GlobalScope.coroutineContext,
            serverValidator = TestServerValidator { Failure("this is an invalid endpoint") },
            config = config
        )
        viewModel.urlChanged("http://example.com")
        val job = viewModel.connect()

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, true)
            assertEquals(canConnect.value, false)
            assertEquals(validationError.value, null)
            assertEquals(sucessfulUrlFound.value, null)
        }
        assertNull(config.serverUrl)

        runBlocking {
            job.join()
        }

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, true)
            assertEquals(error.value, "this is an invalid endpoint")
            assertEquals(validationError.value, null)
            assertEquals(sucessfulUrlFound.value, null)
        }
        assertNull(config.serverUrl)
    }

    @Test
    fun `cancel invalid request`() {
        val config = TestConfig()
        val viewModel = ConnectActivityViewModel(
            GlobalScope.coroutineContext,
            serverValidator = TestServerValidator {
                delay(1)
                Failure("something went wrong, but we cancelled first")
            },
            config = config
        )
        viewModel.urlChanged("http://example.com")
        viewModel.connect()

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, true)
            assertEquals(canConnect.value, false)
            assertEquals(validationError.value, null)
            assertEquals(sucessfulUrlFound.value, null)
        }
        assertNull(config.serverUrl)

        viewModel.cancelConnecting()

        with(viewModel) {
            assertEquals(url.value, "http://example.com")
            assertEquals(isLoading.value, false)
            assertEquals(canConnect.value, true)
            assertEquals(error.value, "")
            assertEquals(validationError.value, null)
            assertEquals(sucessfulUrlFound.value, null)
        }
        assertNull(config.serverUrl)
    }

    @Test
    fun `valid request`() {
        val config = TestConfig()
        val viewModel = ConnectActivityViewModel(
            GlobalScope.coroutineContext,
            serverValidator = TestServerValidator { Success },
            config = config
        )

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
            assertNotNull(sucessfulUrlFound.value)
        }
        assertEquals(config.serverUrl, HttpUrl.parse("http://example.com"))
    }
}
