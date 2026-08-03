package com.flowtask.app.data.repository

import com.flowtask.app.data.local.dao.TagDao
import com.flowtask.app.data.mapper.toDomain
import com.flowtask.app.data.mapper.toEntity
import com.flowtask.app.domain.model.Tag
import com.flowtask.app.domain.repository.TagRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineTagRepository @Inject constructor(
    private val dao: TagDao,
) : TagRepository {
    override fun observeTags(): Flow<List<Tag>> = dao.observeAll().map { tags -> tags.map { it.toDomain() } }
    override suspend fun saveTag(tag: Tag): Long = dao.upsert(tag.toEntity())
    override suspend fun deleteTag(id: Long) = dao.deleteById(id)
}
