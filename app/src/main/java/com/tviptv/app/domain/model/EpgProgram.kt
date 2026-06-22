package com.tviptv.app.domain.model

data class EpgProgram(
    val title: String,
    val description: String?,
    val startMs: Long,
    val endMs: Long,
)

data class NowNextEpg(
    val now: EpgProgram?,
    val next: EpgProgram?,
)
