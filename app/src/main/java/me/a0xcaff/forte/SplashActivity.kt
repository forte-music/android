package me.a0xcaff.forte

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

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
