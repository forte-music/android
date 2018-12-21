package me.a0xcaff.forte

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }
