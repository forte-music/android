package me.a0xcaff.forte.ui

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }
