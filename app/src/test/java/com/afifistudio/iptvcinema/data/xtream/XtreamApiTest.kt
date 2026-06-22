package com.afifistudio.iptvcinema.data.xtream

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class XtreamApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: XtreamApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(XtreamApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun authenticate_parsesActiveUser() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {"user_info":{"auth":1,"status":"Active"}}
                """.trimIndent(),
            ),
        )

        val response = api.authenticate(
            url = server.url("/player_api.php").toString(),
            username = "user",
            password = "pass",
        )

        assertEquals(1, response.userInfo?.auth)
        assertEquals("Active", response.userInfo?.status)
    }

    @Test
    fun getLiveStreams_parsesChannelList() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                [
                  {"stream_id":1,"name":"News HD","stream_icon":"http://logo","category_id":"10"}
                ]
                """.trimIndent(),
            ),
        )

        val streams = api.getLiveStreams(
            url = server.url("/player_api.php").toString(),
            username = "user",
            password = "pass",
        )

        assertEquals(1, streams.size)
        assertEquals("News HD", streams.first().name)
        assertTrue(server.takeRequest().path!!.contains("get_live_streams"))
    }

    @Test
    fun getLiveCategories_parsesCategoryList() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                [
                  {"category_id":"681","category_name":"Sports","parent_id":0}
                ]
                """.trimIndent(),
            ),
        )

        val categories = api.getLiveCategories(
            url = server.url("/player_api.php").toString(),
            username = "user",
            password = "pass",
        )

        assertEquals(1, categories.size)
        assertEquals("681", categories.first().categoryId)
        assertEquals("Sports", categories.first().categoryName)
        assertTrue(server.takeRequest().path!!.contains("get_live_categories"))
    }
}
