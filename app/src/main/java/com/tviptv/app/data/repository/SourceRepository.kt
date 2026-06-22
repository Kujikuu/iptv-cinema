package com.tviptv.app.data.repository

import com.tviptv.app.data.local.dao.CategoryDao
import com.tviptv.app.data.local.dao.ChannelDao
import com.tviptv.app.data.local.dao.EpgDao
import com.tviptv.app.data.local.dao.FavoriteDao
import com.tviptv.app.data.local.dao.LastWatchedDao
import com.tviptv.app.data.local.dao.SourceDao
import com.tviptv.app.data.local.entity.SourceEntity
import com.tviptv.app.data.local.toDomain
import com.tviptv.app.data.prefs.CredentialStore
import com.tviptv.app.domain.model.IptvSourceConfig
import com.tviptv.app.domain.model.SourceType
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
        categoryDao.deleteBySource(id)
        channelDao.deleteBySource(id)
        sourceDao.deleteById(id)
    }
}
