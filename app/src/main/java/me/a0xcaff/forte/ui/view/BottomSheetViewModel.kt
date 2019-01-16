package me.a0xcaff.forte.ui.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.a0xcaff.forte.ui.default

class BottomSheetViewModel : ViewModel() {
    val state = MutableLiveData<@BottomSheetBehavior.State Int>()
        .default(BottomSheetBehavior.STATE_COLLAPSED)
}
