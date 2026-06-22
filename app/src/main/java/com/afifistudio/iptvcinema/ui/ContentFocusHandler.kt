package com.afifistudio.iptvcinema.ui

interface ContentFocusHandler {
    fun requestInitialFocus(): Boolean
    fun canFocusUpToChrome(): Boolean
    fun onChromeFocusGained() {}
    fun onChromeFocusLost() {}
}
