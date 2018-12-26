package me.a0xcaff.forte.ui.connect

import android.os.Bundle
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.util.Util
import me.a0xcaff.forte.MediaPlaybackService
import me.a0xcaff.forte.R
import me.a0xcaff.forte.databinding.ActivityConnectBinding
import me.a0xcaff.forte.ui.makeProgressDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConnectActivity : AppCompatActivity() {
    val viewModel: ConnectActivityViewModel by viewModel()
    private lateinit var binding: ActivityConnectBinding
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect)
        dialog = makeProgressDialog(this)

        val intent = Intent(this, MediaPlaybackService::class.java)
        Util.startForegroundService(this, intent)

        binding.serverUrlInput.editText?.setText(viewModel.url.value)

        viewModel.canConnect.observe(this, Observer {
            binding.connectButton.isEnabled = it
        })

        viewModel.validationError.observe(this, Observer {
            binding.serverUrlInput.error = it
        })

        viewModel.isLoading.observe(this, Observer {
            binding.serverUrlInput.editText?.isEnabled = !it

            if (it) {
                dialog.show()
            } else {
                dialog.hide()
            }
        })

        viewModel.error.observe(this, Observer {
            binding.errorText.text = it
        })

        dialog.setOnCancelListener { viewModel.cancelConnecting() }

        binding.serverUrlInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.urlChanged(s.toString())
            }
        })

        binding.connectButton.setOnClickListener { view ->
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(view.windowToken, 0)

            viewModel.connect()
        }
    }
}
