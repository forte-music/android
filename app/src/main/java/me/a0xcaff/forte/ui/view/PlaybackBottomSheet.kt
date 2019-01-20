package me.a0xcaff.forte.ui.view

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.databinding.ActivityViewBinding

class PlaybackBottomSheet(
    private val binding: ActivityViewBinding,
    private val peekHeight: Int,
    private val onStateChanged: (Int) -> Unit
) {
    private val delegate = BottomSheetBehavior.from(binding.sheet)
    private val peek = binding.sheetPeek
    private val content = binding.sheetContent
    private val peekHeightAsPercent = 1.0f / 8.0f

    private val callback = Callback()

    init {
        delegate.setBottomSheetCallback(callback)
    }

    @BottomSheetBehavior.State
    var state: Int
        get() = delegate.state
        set(newState) {
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> delegate.setPeekHeight(0, true)
                else -> delegate.setPeekHeight(peekHeight)
            }

            updateVisibility(newState)
            delegate.state = newState
        }

    private fun updateVisibility(newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_COLLAPSED -> {
                peek.visibility = View.VISIBLE
                content.visibility = View.GONE
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                peek.visibility = View.GONE
                content.visibility = View.VISIBLE
            }
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

    inner class Callback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val progress = Math.min(slideOffset / peekHeightAsPercent, 1.0f)

            binding.sheetContent.alpha = progress
            binding.sheetPeek.alpha = 1 - progress
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            updateVisibility(newState)
            onStateChanged(newState)

            delegate.state = newState
        }
    }
}
