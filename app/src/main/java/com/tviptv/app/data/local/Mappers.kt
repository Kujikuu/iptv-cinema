package com.tviptv.app.data.local

import com.tviptv.app.data.local.entity.CategoryEntity
import com.tviptv.app.data.local.entity.ChannelEntity
import com.tviptv.app.data.local.entity.SourceEntity
import com.tviptv.app.domain.model.Category
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.IptvSourceConfig

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
