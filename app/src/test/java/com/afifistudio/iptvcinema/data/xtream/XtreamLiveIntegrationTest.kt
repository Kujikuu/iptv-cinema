package com.afifistudio.iptvcinema.data.xtream

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.afifistudio.iptvcinema.ui.setup.SetupDefaults
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Live integration check against the configured default Xtream server.
 */
class XtreamLiveIntegrationTest {

    private val api: XtreamApi = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build(),
        )
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            ),
        )
        .build()
        .create(XtreamApi::class.java)

    @Test
    fun defaultCredentials_authenticateAndLoadChannels() = runBlocking {
        val baseUrl = SetupDefaults.XTREAM_SERVER.trimEnd('/')
        val apiUrl = "$baseUrl/player_api.php"

        val auth = api.authenticate(
            url = apiUrl,
            username = SetupDefaults.XTREAM_USERNAME,
            password = SetupDefaults.XTREAM_PASSWORD,
        )
        assertTrue(
            auth.userInfo?.auth == 1 ||
                auth.userInfo?.status.equals("Active", ignoreCase = true),
        )

        val categories = api.getLiveCategories(
            url = apiUrl,
            username = SetupDefaults.XTREAM_USERNAME,
            password = SetupDefaults.XTREAM_PASSWORD,
        )
        assertTrue(categories.isNotEmpty())

        val streams = api.getLiveStreams(
            url = apiUrl,
            username = SetupDefaults.XTREAM_USERNAME,
            password = SetupDefaults.XTREAM_PASSWORD,
        )
        assertTrue(streams.isNotEmpty())
    }
}
