package com.afifistudio.iptvcinema.ui.setup

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.leanback.widget.GuidanceStylist
import com.afifistudio.iptvcinema.R

object SetupGuidance {
    fun create(
        context: Context,
        title: String,
        description: String,
    ): GuidanceStylist.Guidance =
        GuidanceStylist.Guidance(
            title,
            description,
            context.getString(R.string.app_name),
            ContextCompat.getDrawable(context, R.drawable.app_icon),
        )
}
