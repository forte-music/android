package me.a0xcaff.forte

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.first
import me.a0xcaff.forte.graphql.TestQuery
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

sealed class ValidationResult
object Success : ValidationResult()
data class Failure(val message: String) : ValidationResult()

/**
 * Validates that a server is a valid forte server. It ensures the graphql endpoint is reachable and responds to a query
 * for all songs.
 */
class ServerValidator(private val okHttpClient: OkHttpClient) {
    @UseExperimental(ObsoleteCoroutinesApi::class)
    suspend fun validate(url: HttpUrl): ValidationResult {
        val apolloClient = ApolloClient.builder()
            .okHttpClient(okHttpClient)
            .serverUrl(url)
            .build()

        val testQuery = TestQuery.builder().build()
        val respChannel = apolloClient.query(testQuery).executeAsync()

        val resp = try {
            respChannel.first()
        } catch (e: ApolloException) {
            val message = e.message ?: ""
            return Failure(message)
        } finally {
            respChannel.cancel()
        }

        if (resp.hasErrors()) {
            val message = resp.errors().joinToString(separator = "\n", transform = { it.message() ?: "" })
            return Failure(message)
        }

        return Success
    }
}
