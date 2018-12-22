package me.a0xcaff.forte

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun ProgressDialogBuilder(context: Context): AlertDialog =
    AlertDialog.Builder(context).apply {
        setView(R.layout.progress_dialog)
    }.create()
