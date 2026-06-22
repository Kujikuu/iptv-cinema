package com.afifistudio.iptvcinema.ui.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupFormHelperTest {

    @Test
    fun hostLabel_extractsHostFromUrl() {
        assertEquals("Servx", SetupFormHelper.hostLabel("http://servx.pro:80"))
    }

    @Test
    fun hostLabel_returnsEmptyForInvalidUrl() {
        assertEquals("", SetupFormHelper.hostLabel(""))
    }

    @Test
    fun xtreamFormState_resolvedName_prefersExplicitName() {
        val form = XtreamFormState(
            name = "My List",
            serverUrl = SetupDefaults.XTREAM_SERVER,
            username = SetupDefaults.XTREAM_USERNAME,
            password = SetupDefaults.XTREAM_PASSWORD,
        )
        assertEquals("My List", form.resolvedName())
    }

    @Test
    fun xtreamFormState_resolvedName_fallsBackToUsername() {
        val form = XtreamFormState(
            name = "  ",
            serverUrl = SetupDefaults.XTREAM_SERVER,
            username = "ahmed-afifi",
            password = SetupDefaults.XTREAM_PASSWORD,
        )
        assertEquals("ahmed-afifi", form.resolvedName())
    }

    @Test
    fun xtreamFormState_resolvedName_fallsBackToHostLabel() {
        val form = XtreamFormState(
            name = "",
            serverUrl = "http://servx.pro:80",
            username = "",
            password = SetupDefaults.XTREAM_PASSWORD,
        )
        assertEquals("Servx", form.resolvedName())
    }

    @Test
    fun setupDefaults_containsNonBlankValues() {
        assertTrue(SetupDefaults.XTREAM_SERVER.isNotBlank())
        assertTrue(SetupDefaults.XTREAM_USERNAME.isNotBlank())
        assertTrue(SetupDefaults.XTREAM_PASSWORD.isNotBlank())
    }
}
