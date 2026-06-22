package com.afifistudio.iptvcinema.ui

import android.view.View
import androidx.fragment.app.Fragment
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.ui.browse.CardFocusHelper

fun Fragment.replaceContent(fragment: Fragment) {
    resetFocusedCardState(activity?.currentFocus)
    parentFragmentManager.beginTransaction()
        .replace(R.id.main_container, fragment)
        .addToBackStack(null)
        .commit()
    parentFragmentManager.executePendingTransactions()
}

fun Fragment.startContentActivity(intent: android.content.Intent) {
    resetFocusedCardState(activity?.currentFocus)
    startActivity(intent)
}

private fun resetFocusedCardState(focus: View?) {
    var view = focus
    repeat(4) {
        if (view == null) return
        CardFocusHelper.resetFocus(view)
        view = view.parent as? View
    }
    focus?.clearFocus()
}
