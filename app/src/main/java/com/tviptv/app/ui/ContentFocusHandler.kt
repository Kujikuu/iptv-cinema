package com.tviptv.app.ui

interface ContentFocusHandler {
    fun requestInitialFocus(): Boolean
    fun canFocusUpToChrome(): Boolean
    fun onChromeFocusGained() {}
    fun onChromeFocusLost() {}
}
