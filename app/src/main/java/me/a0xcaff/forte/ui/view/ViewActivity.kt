package me.a0xcaff.forte.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet)
        val peek = binding.sheetPeek
        val content = binding.sheetContent

        val peekHeightAsPercent = 1.0f / 8.0f
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val progress = Math.min(slideOffset / peekHeightAsPercent, 1.0f)

                content.alpha = progress
                peek.alpha = 1 - progress
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        content.visibility = View.GONE
                    BottomSheetBehavior.STATE_EXPANDED ->
                        peek.visibility = View.GONE
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        peek.visibility = View.GONE
                        content.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_SETTLING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        peek.visibility = View.VISIBLE
                        content.visibility = View.VISIBLE
                    }
                }
            }
        })
    }
}
