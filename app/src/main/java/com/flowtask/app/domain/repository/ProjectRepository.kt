package com.flowtask.app.domain.repository

import com.flowtask.app.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeProjects(): Flow<List<Project>>
    suspend fun saveProject(project: Project): Long
    suspend fun deleteProject(id: Long)
}
