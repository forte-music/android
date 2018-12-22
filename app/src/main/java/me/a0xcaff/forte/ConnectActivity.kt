package me.a0xcaff.forte

import android.os.Bundle
import android.content.Context
import androidx.databinding.DataBindingUtil
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import me.a0xcaff.forte.databinding.ActivityConnectBinding

class ConnectActivity : AppCompatActivity() {
    private lateinit var viewModel: ConnectActivityViewModel
    private lateinit var binding: ActivityConnectBinding
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ConnectActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect)
        dialog = ProgressDialogBuilder(this)

        binding.serverUrlInput.editText?.setText(viewModel.url.value)

        viewModel.canConnect.observe(this, Observer {
            binding.connectButton.isEnabled = it
        })

        viewModel.error.observe(this, Observer {
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
