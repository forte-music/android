package me.a0xcaff.forte.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.a0xcaff.forte.ui.connect.ConnectActivity
import me.a0xcaff.forte.ui.view.ViewActivity
import org.koin.android.ext.android.get

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val redirector = LaunchRedirector(get(), {
            startActivity(Intent(this, ConnectActivity::class.java))
            finish()
        }, {
            startActivity(Intent(this, ViewActivity::class.java))
            finish()
        })

        lifecycle.addObserver(redirector)
    }
}
