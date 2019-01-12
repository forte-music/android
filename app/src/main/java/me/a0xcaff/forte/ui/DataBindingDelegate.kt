package me.a0xcaff.forte.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.reflect.KProperty

class ActivityDataBindingDelegate<TBinding : ViewDataBinding>(appCompatActivity: AppCompatActivity, layoutId: Int) {
    private val binding: TBinding by lazy { DataBindingUtil.setContentView<TBinding>(appCompatActivity, layoutId) }

    init {
        appCompatActivity.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                binding
            }
        })
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): TBinding =
        binding
}

fun <TBinding : ViewDataBinding> AppCompatActivity.dataBinding(layoutId: Int): ActivityDataBindingDelegate<TBinding> =
    ActivityDataBindingDelegate(this, layoutId)

