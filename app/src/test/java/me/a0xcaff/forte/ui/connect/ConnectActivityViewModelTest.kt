/* ktlint-disable no-wildcard-imports */
package me.a0xcaff.forte.ui.connect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule

class ConnectActivityViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `initial state`() {
        val viewModel = ConnectActivityViewModel()

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
        val viewModel = ConnectActivityViewModel()
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
        val viewModel = ConnectActivityViewModel(GlobalScope.coroutineContext)
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
            assertNotEquals(error.value, "")
            assertEquals(validationError.value, null)
        }
    }

    @Test
    fun `cancel invalid request`() {
        val viewModel = ConnectActivityViewModel(GlobalScope.coroutineContext)
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
}
