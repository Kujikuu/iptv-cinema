package com.afifistudio.iptvcinema.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class RefreshInterval(val days: Int) {
    MANUAL(0),
    ONE_DAY(1),
    TWO_DAYS(2),
    SEVEN_DAYS(7),
    ;

    companion object {
        fun fromDays(days: Int): RefreshInterval =
            entries.firstOrNull { it.days == days } ?: TWO_DAYS
    }
}

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRefreshInterval(): RefreshInterval =
        RefreshInterval.fromDays(prefs.getInt(KEY_REFRESH_INTERVAL_DAYS, RefreshInterval.TWO_DAYS.days))

    fun setRefreshInterval(interval: RefreshInterval) {
        prefs.edit().putInt(KEY_REFRESH_INTERVAL_DAYS, interval.days).apply()
    }

    fun isAutoplayNextEpisodeEnabled(): Boolean =
        prefs.getBoolean(KEY_AUTOPLAY_NEXT_EPISODE, true)

    fun setAutoplayNextEpisodeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOPLAY_NEXT_EPISODE, enabled).apply()
    }

    fun toggleAutoplayNextEpisode(): Boolean {
        val enabled = !isAutoplayNextEpisodeEnabled()
        setAutoplayNextEpisodeEnabled(enabled)
        return enabled
    }

    fun cycleRefreshInterval(): RefreshInterval {
        val currentIndex = REFRESH_ORDER.indexOf(getRefreshInterval())
        val next = REFRESH_ORDER[(currentIndex + 1) % REFRESH_ORDER.size]
        setRefreshInterval(next)
        return next
    }

    fun getSelectedSourceId(): Long? {
        val id = prefs.getLong(KEY_SELECTED_SOURCE_ID, NO_SOURCE_ID)
        return id.takeIf { it != NO_SOURCE_ID }
    }

    fun setSelectedSourceId(sourceId: Long?) {
        prefs.edit()
            .putLong(KEY_SELECTED_SOURCE_ID, sourceId ?: NO_SOURCE_ID)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "iptv_app_settings"
        private const val KEY_REFRESH_INTERVAL_DAYS = "refresh_interval_days"
        private const val KEY_AUTOPLAY_NEXT_EPISODE = "autoplay_next_episode"
        private const val KEY_SELECTED_SOURCE_ID = "selected_source_id"
        private const val NO_SOURCE_ID = -1L

        val REFRESH_ORDER = listOf(
            RefreshInterval.MANUAL,
            RefreshInterval.ONE_DAY,
            RefreshInterval.TWO_DAYS,
            RefreshInterval.SEVEN_DAYS,
        )
    }
}
