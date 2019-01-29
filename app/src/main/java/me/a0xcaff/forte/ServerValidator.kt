package me.a0xcaff.forte

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import me.a0xcaff.forte.graphql.TestQuery
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

sealed class ValidationResult
object Success : ValidationResult()
data class Failure(val message: String) : ValidationResult()

interface ServerValidator {
    suspend fun validate(url: HttpUrl): ValidationResult
}

/**
 * Validates that a server is a valid forte server. It ensures the graphql endpoint is reachable and responds to a query
 * for all songs.
 */
class ServerValidatorImpl(private val okHttpClient: OkHttpClient) : ServerValidator {
    override suspend fun validate(url: HttpUrl): ValidationResult {
        val apolloClient = ApolloClient.builder()
            .okHttpClient(okHttpClient)
            .serverUrl(url)
            .build()

        val testQuery = TestQuery.builder().build()
        val resp = try {
            apolloClient.query(testQuery).toDeferred().await()
        } catch (e: ApolloException) {
            val message = e.message ?: ""
            return Failure(message)
        }

        if (resp.hasErrors()) {
            val message = resp.errors().joinToString(separator = "\n", transform = { it.message() ?: "" })
            return Failure(message)
        }

        return Success
    }
}
