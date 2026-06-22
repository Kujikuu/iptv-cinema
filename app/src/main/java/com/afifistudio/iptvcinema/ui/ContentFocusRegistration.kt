package com.afifistudio.iptvcinema.ui

import androidx.fragment.app.Fragment

fun Fragment.registerContentFocusHandler(handler: ContentFocusHandler) {
    (activity as? HomeChromeHost)?.registerContentFocusHandler(handler)
}

fun Fragment.unregisterContentFocusHandler() {
    (activity as? HomeChromeHost)?.registerContentFocusHandler(null)
}

fun Fragment.requestChromeFocus(): Boolean =
    (activity as? HomeChromeHost)?.requestChromeFocus() == true
