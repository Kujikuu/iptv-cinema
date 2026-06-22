package com.afifistudio.iptvcinema.ui.browse

import com.afifistudio.iptvcinema.domain.model.Channel
import org.junit.Assert.assertEquals
import org.junit.Test

class BrowseItemTest {

    @Test
    fun browseItem_storesChannelForNavigation() {
        val channel = Channel(
            id = "490579",
            name = "Match",
            logoUrl = "http://logo.png",
            categoryId = "681",
            categoryName = "Sports",
            streamUrl = null,
            sourceId = 1L,
            externalId = "490579",
        )

        val item = BrowseItem(
            id = "channel_490579",
            title = channel.name,
            subtitle = channel.categoryName,
            imageUrl = channel.logoUrl,
            type = BrowseItemType.CHANNEL,
            channel = channel,
            badge = "LIVE",
        )

        assertEquals(BrowseItemType.CHANNEL, item.type)
        assertEquals("490579", item.channel?.id)
        assertEquals("Match", item.title)
    }
}
