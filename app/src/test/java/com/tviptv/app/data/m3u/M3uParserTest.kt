package com.tviptv.app.data.m3u

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class M3uParserTest {

    private lateinit var parser: M3uParser

    @Before
    fun setUp() {
        parser = M3uParser()
    }

    @Test
    fun parseSamplePlaylist_extractsChannelsAndCategories() {
        val content = readFixture("sample.m3u")
        val result = parser.parse(content)

        assertEquals(3, result.entries.size)
        assertEquals("BBC One", result.entries[0].name)
        assertEquals("http://example.com/stream/bbc1.m3u8", result.entries[0].streamUrl)
        assertEquals("bbc1.uk", result.entries[0].tvgId)
        assertEquals("https://example.com/bbc1.png", result.entries[0].logoUrl)
        assertEquals("News", result.entries[0].groupTitle)

        assertTrue(result.categories.containsKey("News"))
        assertTrue(result.categories.containsKey("Sports"))
    }

    @Test
    fun parse_deduplicatesByStreamUrl() {
        val content = """
            #EXTM3U
            #EXTINF:-1,Channel A
            http://example.com/same
            #EXTINF:-1,Channel B
            http://example.com/same
        """.trimIndent()

        val result = parser.parse(content)
        assertEquals(1, result.entries.size)
    }

    @Test
    fun parse_assignsDefaultCategoryWhenMissing() {
        val content = """
            #EXTM3U
            #EXTINF:-1,No Group Channel
            http://example.com/nogroup
        """.trimIndent()

        val result = parser.parse(content)
        assertEquals(null, result.entries.first().groupTitle)
        assertTrue(result.categories.containsKey(M3uParser.DEFAULT_CATEGORY))
    }

    @Test
    fun parse_extractsChannelNumberFromTvgChno() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-chno="101" tvg-id="news1",News HD
            http://example.com/news
        """.trimIndent()

        val result = parser.parse(content)
        assertEquals(101, result.entries.first().channelNumber)
    }

    private fun readFixture(name: String): String {
        val url = javaClass.classLoader?.getResource(name)
            ?: error("Missing fixture $name")
        return File(url.toURI()).readText()
    }
}
