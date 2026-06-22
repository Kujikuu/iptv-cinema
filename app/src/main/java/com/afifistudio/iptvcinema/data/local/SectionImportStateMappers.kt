package com.afifistudio.iptvcinema.data.local

import com.afifistudio.iptvcinema.data.local.entity.SectionImportStateEntity
import com.afifistudio.iptvcinema.domain.model.SectionImportState

fun SectionImportStateEntity.toDomain(): SectionImportState =
    SectionImportState(
        sourceId = sourceId,
        contentType = contentType,
        status = status,
        updatedAt = updatedAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        errorMessage = errorMessage,
    )
