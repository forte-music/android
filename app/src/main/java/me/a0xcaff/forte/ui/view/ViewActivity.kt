package me.a0xcaff.forte.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.util.Util
import me.a0xcaff.forte.Config
import me.a0xcaff.forte.MediaPlaybackService
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.ActivityViewBinding
import org.koin.android.ext.android.get

class ViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view)

        val config = get<Config>()
        binding.serverUrl.text = config.serverUrl.toString()

        Util.startForegroundService(this, Intent(this, MediaPlaybackService::class.java))
    }
}
