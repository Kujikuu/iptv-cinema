package com.tviptv.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun normalizeBaseUrl_addsHttpPrefixWhenMissing() {
        assertEquals("http://servx.pro:80", normalizeBaseUrl("servx.pro:80"))
    }

    @Test
    fun normalizeBaseUrl_trimsWhitespaceAndTrailingSlash() {
        assertEquals("http://servx.pro:80", normalizeBaseUrl("  http://servx.pro:80/  "))
    }

    @Test
    fun normalizeBaseUrl_preservesHttps() {
        assertEquals("https://example.com", normalizeBaseUrl("https://example.com/"))
    }

    @Test
    fun buildXtreamStreamUrl_buildsExpectedLiveUrl() {
        val url = buildXtreamStreamUrl(
            baseUrl = "http://servx.pro:80/",
            username = "ahmed-afifi",
            password = "01091072705",
            streamId = "490579",
        )
        assertEquals(
            "http://servx.pro:80/live/ahmed-afifi/01091072705/490579.m3u8",
            url,
        )
    }

    @Test
    fun buildXtreamStreamUrl_supportsCustomExtension() {
        val url = buildXtreamStreamUrl(
            baseUrl = "http://servx.pro:80",
            username = "user",
            password = "pass",
            streamId = "1",
            extension = "ts",
        )
        assertEquals("http://servx.pro:80/live/user/pass/1.ts", url)
    }
}
