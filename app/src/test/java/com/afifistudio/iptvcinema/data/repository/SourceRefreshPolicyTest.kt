package com.afifistudio.iptvcinema.data.repository

import com.afifistudio.iptvcinema.data.prefs.AppPreferences
import com.afifistudio.iptvcinema.data.prefs.RefreshInterval
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SourceRefreshPolicyTest {

    private val appPreferences = mockk<AppPreferences>()
    private lateinit var policy: SourceRefreshPolicy

    @Before
    fun setUp() {
        policy = SourceRefreshPolicy(appPreferences)
    }

    @Test
    fun shouldRefreshFromNetwork_whenCacheEmpty() {
        every { appPreferences.getRefreshInterval() } returns RefreshInterval.TWO_DAYS

        assertTrue(policy.shouldRefreshFromNetwork(channelCount = 0, sourceUpdatedAt = 1_000L))
    }

    @Test
    fun shouldRefreshFromNetwork_whenStaleAndIntervalSet() {
        every { appPreferences.getRefreshInterval() } returns RefreshInterval.TWO_DAYS
        val now = System.currentTimeMillis()
        val updatedAt = now - (3 * DAY_MS)

        assertTrue(policy.isRefreshDue(updatedAt, now))
        assertTrue(policy.shouldRefreshFromNetwork(channelCount = 100, sourceUpdatedAt = updatedAt))
    }

    @Test
    fun shouldNotRefreshFromNetwork_whenFreshCache() {
        every { appPreferences.getRefreshInterval() } returns RefreshInterval.TWO_DAYS
        val now = System.currentTimeMillis()
        val updatedAt = now - DAY_MS

        assertFalse(policy.isRefreshDue(updatedAt, now))
        assertFalse(policy.shouldRefreshFromNetwork(channelCount = 100, sourceUpdatedAt = updatedAt))
    }

    @Test
    fun shouldNotAutoRefresh_whenManualInterval() {
        every { appPreferences.getRefreshInterval() } returns RefreshInterval.MANUAL
        val now = System.currentTimeMillis()
        val updatedAt = now - (10 * DAY_MS)

        assertFalse(policy.isRefreshDue(updatedAt, now))
        assertFalse(policy.shouldRefreshFromNetwork(channelCount = 100, sourceUpdatedAt = updatedAt))
    }

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
