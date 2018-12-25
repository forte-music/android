package me.a0xcaff.forte.ui

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import me.a0xcaff.forte.R

fun makeProgressDialog(activity: AppCompatActivity): AlertDialog {
    val dialog = AlertDialog.Builder(activity).apply {
        setView(R.layout.progress_dialog)
    }.create()

    activity.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun destroy() {
            dialog.dismiss()
        }
    })

    return dialog
}
