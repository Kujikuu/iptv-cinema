package com.afifistudio.iptvcinema.data.repository

import com.afifistudio.iptvcinema.data.local.dao.SectionImportStateDao
import com.afifistudio.iptvcinema.data.local.entity.SectionImportStateEntity
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.SectionImportStatus
import com.afifistudio.iptvcinema.domain.repository.IptvRepositoryFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceImportCoordinator @Inject constructor(
    private val sectionImportStateDao: SectionImportStateDao,
    private val repositoryFactory: IptvRepositoryFactory,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val activeJobs = mutableMapOf<ImportKey, Job>()
    private val activeSourceJobs = mutableMapOf<Long, Job>()

    suspend fun queueXtreamInitialImport(sourceId: Long) {
        val now = System.currentTimeMillis()
        sectionImportStateDao.upsertAll(
            IMPORT_CONTENT_TYPES.map { contentType ->
                SectionImportStateEntity(
                    sourceId = sourceId,
                    contentType = contentType,
                    status = SectionImportStatus.QUEUED,
                    updatedAt = now,
                )
            },
        )
        startPendingImports(sourceId)
    }

    suspend fun ensurePendingImportsRunning(sourceId: Long) {
        val states = sectionImportStateDao.getBySource(sourceId)
        if (states.isEmpty()) return
        val hasPending = states.any {
            it.status == SectionImportStatus.QUEUED || it.status == SectionImportStatus.LOADING
        }
        if (hasPending) startPendingImports(sourceId)
    }

    suspend fun refreshSection(sourceId: Long, contentType: ContentType): Result<Unit> {
        sectionImportStateDao.upsert(
            SectionImportStateEntity(
                sourceId = sourceId,
                contentType = contentType,
                status = SectionImportStatus.QUEUED,
            ),
        )
        val key = ImportKey(sourceId, contentType)
        val job = launchImportIfNeeded(key, retryErrors = true)
        job.join()
        return when (sectionImportStateDao.get(sourceId, contentType)?.status) {
            SectionImportStatus.READY -> Result.success(Unit)
            else -> Result.failure(
                IllegalStateException(
                    sectionImportStateDao.get(sourceId, contentType)?.errorMessage ?: "Refresh failed",
                ),
            )
        }
    }

    private suspend fun startPendingImports(sourceId: Long) {
        mutex.withLock {
            activeSourceJobs[sourceId]?.takeIf { it.isActive }?.let { return }
            val job = scope.launch {
                IMPORT_CONTENT_TYPES.forEach { contentType ->
                    launchImportIfNeeded(ImportKey(sourceId, contentType), retryErrors = false).join()
                }
            }
            activeSourceJobs[sourceId] = job
            job.invokeOnCompletion {
                scope.launch {
                    mutex.withLock {
                        if (activeSourceJobs[sourceId] == job) {
                            activeSourceJobs.remove(sourceId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun launchImportIfNeeded(key: ImportKey, retryErrors: Boolean): Job {
        mutex.withLock {
            activeJobs[key]?.takeIf { it.isActive }?.let { return it }
            val state = sectionImportStateDao.get(key.sourceId, key.contentType)
            val shouldRun = when (state?.status) {
                SectionImportStatus.QUEUED,
                SectionImportStatus.LOADING -> true
                SectionImportStatus.ERROR -> retryErrors
                SectionImportStatus.READY,
                null -> false
            }
            if (!shouldRun) return CompletableDeferred(Unit)

            val job = scope.launch { importSection(key) }
            activeJobs[key] = job
            job.invokeOnCompletion {
                scope.launch {
                    mutex.withLock {
                        if (activeJobs[key] == job) {
                            activeJobs.remove(key)
                        }
                    }
                }
            }
            return job
        }
    }

    private suspend fun importSection(key: ImportKey) {
        val startedAt = System.currentTimeMillis()
        sectionImportStateDao.upsert(
            SectionImportStateEntity(
                sourceId = key.sourceId,
                contentType = key.contentType,
                status = SectionImportStatus.LOADING,
                updatedAt = startedAt,
                startedAt = startedAt,
            ),
        )

        val result = repositoryFactory
            .forSource(key.sourceId)
            .refreshSection(key.sourceId, key.contentType)

        val finishedAt = System.currentTimeMillis()
        sectionImportStateDao.upsert(
            SectionImportStateEntity(
                sourceId = key.sourceId,
                contentType = key.contentType,
                status = if (result.isSuccess) SectionImportStatus.READY else SectionImportStatus.ERROR,
                updatedAt = finishedAt,
                startedAt = startedAt,
                finishedAt = finishedAt,
                errorMessage = result.exceptionOrNull()?.message,
            ),
        )
    }

    private data class ImportKey(
        val sourceId: Long,
        val contentType: ContentType,
    )

    companion object {
        val IMPORT_CONTENT_TYPES = listOf(ContentType.LIVE, ContentType.MOVIE, ContentType.SERIES)
    }
}
