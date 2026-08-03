package com.flowtask.app.domain.repository

import com.flowtask.app.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>
    suspend fun saveTag(tag: Tag): Long
    suspend fun deleteTag(id: Long)
}
