package com.afifistudio.iptvcinema.domain.model

enum class SectionImportStatus {
    QUEUED,
    LOADING,
    READY,
    ERROR,
}

data class SectionImportState(
    val sourceId: Long,
    val contentType: ContentType,
    val status: SectionImportStatus,
    val updatedAt: Long,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val errorMessage: String? = null,
)
