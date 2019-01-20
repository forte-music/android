package me.a0xcaff.forte

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

class MergedLiveData<T>(
    val a: LiveData<T>,
    val b: LiveData<T>
) : MediatorLiveData<T>() {
    init {
        if (a.value != null) {
            value = a.value
        } else if (b.value != null) {
            value = b.value
        }

        addSource(a) {
            if (it != null) {
                value = it
            }
        }
        addSource(b) {
            if (it != null) {
                value = it
            }
        }
    }
}

fun <X, Y> LiveData<X>.map(mapFn: (X) -> Y): LiveData<Y> =
    Transformations.map(this, mapFn)

fun <X, Y> LiveData<X>.switchMap(mapFn: (X) -> LiveData<Y>?): LiveData<Y> =
    Transformations.switchMap(this, mapFn)
