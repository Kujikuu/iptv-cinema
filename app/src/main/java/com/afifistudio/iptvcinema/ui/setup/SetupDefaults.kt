package com.afifistudio.iptvcinema.ui.setup

object SetupDefaults {
    const val XTREAM_NAME = "Servx IPTV"
    const val XTREAM_SERVER = "http://servx.pro:80"
    const val XTREAM_USERNAME = "ahmed-afifi"
    const val XTREAM_PASSWORD = "01091072705"
}

data class XtreamFormState(
    var name: String = SetupDefaults.XTREAM_NAME,
    var serverUrl: String = SetupDefaults.XTREAM_SERVER,
    var username: String = SetupDefaults.XTREAM_USERNAME,
    var password: String = SetupDefaults.XTREAM_PASSWORD,
) {
    fun resolvedName(): String =
        name.trim().ifBlank {
            username.trim().ifBlank {
                SetupFormHelper.hostLabel(serverUrl).ifBlank { "IPTV" }
            }
        }
}
