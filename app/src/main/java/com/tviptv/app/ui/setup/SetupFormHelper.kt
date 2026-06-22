package com.tviptv.app.ui.setup

import androidx.leanback.widget.GuidedAction

internal object SetupFormHelper {

    fun actionText(action: GuidedAction?): String =
        action?.description?.toString()?.trim().orEmpty()

    fun hostLabel(serverUrl: String): String =
        runCatching {
            val host = serverUrl.trim()
                .removePrefix("http://")
                .removePrefix("https://")
                .substringBefore('/')
                .substringBefore(':')
            host.substringBefore('.')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }.getOrNull().orEmpty().ifBlank { "" }
}
