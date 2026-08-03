package com.flowtask.app.data.repository

import com.flowtask.app.data.local.dao.ProjectDao
import com.flowtask.app.data.mapper.toDomain
import com.flowtask.app.data.mapper.toEntity
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.repository.ProjectRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineProjectRepository @Inject constructor(
    private val dao: ProjectDao,
) : ProjectRepository {
    override fun observeProjects(): Flow<List<Project>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveProject(project: Project): Long {
        require(project.name.isNotBlank()) { "Project name cannot be blank" }
        require(project.endDate == null || !project.endDate.isBefore(project.startDate)) {
            "Project end date cannot be before its start date"
        }
        return dao.upsert(project.toEntity())
    }

    override suspend fun deleteProject(id: Long) = dao.deleteById(id)
}
