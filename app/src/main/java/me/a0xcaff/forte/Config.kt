package me.a0xcaff.forte

import android.content.Context
import android.content.SharedPreferences
import okhttp3.HttpUrl

private const val SERVER_URL = "server_url"

interface Config {
    var serverUrl: HttpUrl?
}

class ConfigImpl(private val prefs: SharedPreferences) : Config {
    override var serverUrl: HttpUrl?
        get() {
            val raw = prefs.getString(SERVER_URL, null) ?: return null
            return HttpUrl.parse(raw)!!
        }
        set(value) {
            with(prefs.edit()) {
                putString(SERVER_URL, value.toString())
                apply()
            }
        }

    companion object {
        fun from(ctx: Context): ConfigImpl =
            ConfigImpl(ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    }
}
