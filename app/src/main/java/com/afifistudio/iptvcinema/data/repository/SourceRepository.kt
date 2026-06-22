package com.afifistudio.iptvcinema.data.repository

import com.afifistudio.iptvcinema.data.local.dao.CategoryDao
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.EpgDao
import com.afifistudio.iptvcinema.data.local.dao.FavoriteDao
import com.afifistudio.iptvcinema.data.local.dao.LastWatchedDao
import com.afifistudio.iptvcinema.data.local.dao.SectionImportStateDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.SourceEntity
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.data.prefs.CredentialStore
import com.afifistudio.iptvcinema.domain.model.IptvSourceConfig
import com.afifistudio.iptvcinema.domain.model.SourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepository @Inject constructor(
    private val sourceDao: SourceDao,
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val favoriteDao: FavoriteDao,
    private val lastWatchedDao: LastWatchedDao,
    private val epgDao: EpgDao,
    private val sectionImportStateDao: SectionImportStateDao,
    private val credentialStore: CredentialStore,
) {
    fun observeSources(): Flow<List<IptvSourceConfig>> =
        sourceDao.observeAll().map { sources ->
            sources.map { it.toDomain(credentialStore.getPassword(it.id)) }
        }

    suspend fun getSources(): List<IptvSourceConfig> =
        sourceDao.getAll().map { it.toDomain(credentialStore.getPassword(it.id)) }

    suspend fun hasSources(): Boolean = sourceDao.count() > 0

    suspend fun getSource(id: Long): IptvSourceConfig? =
        sourceDao.getById(id)?.toDomain(credentialStore.getPassword(id))

    suspend fun addSource(
        type: SourceType,
        name: String,
        url: String?,
        username: String?,
        password: String?,
    ): Long {
        val id = sourceDao.insert(
            SourceEntity(
                type = type,
                name = name,
                url = url,
                username = username,
            ),
        )
        if (!password.isNullOrBlank()) {
            credentialStore.savePassword(id, password)
        }
        return id
    }

    suspend fun deleteSource(id: Long) {
        credentialStore.deletePassword(id)
        favoriteDao.deleteBySource(id)
        lastWatchedDao.deleteBySource(id)
        epgDao.deleteBySource(id)
        sectionImportStateDao.deleteBySource(id)
        categoryDao.deleteBySource(id)
        channelDao.deleteBySource(id)
        sourceDao.deleteById(id)
    }
}
