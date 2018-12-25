package me.a0xcaff.forte.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.a0xcaff.forte.Config
import me.a0xcaff.forte.LaunchRedirector
import me.a0xcaff.forte.R
import me.a0xcaff.forte.ui.connect.ConnectActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val config = Config.getConfig(this)
        val redirector = LaunchRedirector(config) {
            startActivity(Intent(this, ConnectActivity::class.java))
        }

        lifecycle.addObserver(redirector)
    }
}
