package com.tviptv.app.data.local

import com.tviptv.app.data.local.entity.CategoryEntity
import com.tviptv.app.data.local.entity.ChannelEntity
import com.tviptv.app.data.local.entity.SourceEntity
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun sourceEntity_toDomain_mapsFieldsAndPassword() {
        val entity = SourceEntity(
            id = 7L,
            type = SourceType.XTREAM,
            name = "Servx IPTV",
            url = "http://servx.pro:80",
            username = "ahmed-afifi",
        )

        val domain = entity.toDomain(password = "secret")

        assertEquals(7L, domain.id)
        assertEquals(SourceType.XTREAM, domain.type)
        assertEquals("Servx IPTV", domain.name)
        assertEquals("http://servx.pro:80", domain.url)
        assertEquals("ahmed-afifi", domain.username)
        assertEquals("secret", domain.password)
    }

    @Test
    fun categoryEntity_toDomain_usesExternalIdAsCategoryId() {
        val entity = CategoryEntity(
            sourceId = 1L,
            externalId = "681",
            name = "Sports",
            contentType = ContentType.LIVE,
        )

        val domain = entity.toDomain()

        assertEquals("681", domain.id)
        assertEquals("Sports", domain.name)
        assertEquals(1L, domain.sourceId)
    }

    @Test
    fun channelEntity_toDomain_mapsCategoryNameWhenProvided() {
        val entity = ChannelEntity(
            sourceId = 1L,
            externalId = "490579",
            name = "Saudi Arabia vs Spain",
            logoUrl = "http://logo.png",
            categoryId = "681",
            streamUrl = null,
            sortOrder = 0,
            contentType = ContentType.LIVE,
        )

        val domain = entity.toDomain(categoryName = "Replay")

        assertEquals("490579", domain.id)
        assertEquals("490579", domain.externalId)
        assertEquals("Saudi Arabia vs Spain", domain.name)
        assertEquals("681", domain.categoryId)
        assertEquals("Replay", domain.categoryName)
        assertEquals(1L, domain.sourceId)
    }
}
