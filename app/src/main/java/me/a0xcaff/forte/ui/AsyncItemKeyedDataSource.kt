package me.a0xcaff.forte.ui

import androidx.paging.ItemKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class AsyncItemKeyedDataSource<Key, Value>(
    private val scope: CoroutineScope
) : ItemKeyedDataSource<Key, Value>() {
    override fun loadInitial(params: LoadInitialParams<Key>, callback: LoadInitialCallback<Value>) {
        scope.launch {
            val result = loadInitial(params)
            callback.onResult(result.data, 0, result.totalCount)
        }
    }

    override fun loadAfter(params: LoadParams<Key>, callback: LoadCallback<Value>) {
        scope.launch {
            callback.onResult(loadAfter(params))
        }
    }

    override fun loadBefore(params: LoadParams<Key>, callback: LoadCallback<Value>) {
    }

    abstract suspend fun loadInitial(params: LoadInitialParams<Key>): LoadInitialResult<Value>
    abstract suspend fun loadAfter(params: LoadParams<Key>): List<Value>

    data class LoadInitialResult<Value>(
        val data: List<Value>,
        val totalCount: Int
    )
}