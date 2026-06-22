package com.afifistudio.iptvcinema.data.local

import com.afifistudio.iptvcinema.data.local.entity.CategoryEntity
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.data.local.entity.SourceEntity
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.IptvSourceConfig

fun SourceEntity.toDomain(password: String? = null) = IptvSourceConfig(
    id = id,
    type = type,
    name = name,
    url = url,
    username = username,
    password = password,
    updatedAt = updatedAt,
)

fun CategoryEntity.toDomain() = Category(
    id = externalId,
    name = name,
    sourceId = sourceId,
    contentType = contentType,
)

fun ChannelEntity.toDomain(categoryName: String? = null) = Channel(
    id = externalId,
    name = name,
    logoUrl = logoUrl,
    categoryId = categoryId,
    categoryName = categoryName,
    streamUrl = streamUrl,
    sourceId = sourceId,
    externalId = externalId,
    contentType = contentType,
    containerExtension = containerExtension,
    plot = plot,
    addedAt = addedAt,
    channelNumber = channelNumber,
)
