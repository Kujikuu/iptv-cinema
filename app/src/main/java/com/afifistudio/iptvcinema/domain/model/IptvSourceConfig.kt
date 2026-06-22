package com.afifistudio.iptvcinema.domain.model

data class IptvSourceConfig(
    val id: Long,
    val type: SourceType,
    val name: String,
    val url: String?,
    val username: String?,
    val password: String?,
    val updatedAt: Long = System.currentTimeMillis(),
)
