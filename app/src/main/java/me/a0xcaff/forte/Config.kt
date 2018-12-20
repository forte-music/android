package me.a0xcaff.forte

import android.content.Context
import android.content.SharedPreferences

const val SERVER_URL = "server_url"

class Config(private val prefs: SharedPreferences) {
    fun getServerUrl(): String? =
        prefs.getString(SERVER_URL, null)

    fun setServerUrl(new: String) {
        prefs.edit().putString(SERVER_URL, new).apply()
    }

    companion object {
        fun getConfig(ctx: Context): Config =
            Config(ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    }
}
