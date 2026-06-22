package com.tviptv.app.ui

enum class HomeChromeMode {
    LAUNCHER,
    BROWSE,
}

interface HomeChromeHost {
    fun setChromeVisible(visible: Boolean)
    fun setChromeMode(mode: HomeChromeMode)
    fun setChromeFooterVisible(visible: Boolean)
    fun setBackdropImageUrl(url: String?)
    fun setOnSearchClickListener(listener: (() -> Unit)?)
    fun setOnSettingsClickListener(listener: (() -> Unit)?)
    fun registerContentFocusHandler(handler: ContentFocusHandler?)
    fun requestChromeFocus(): Boolean
    fun requestContentFocus(): Boolean
}
