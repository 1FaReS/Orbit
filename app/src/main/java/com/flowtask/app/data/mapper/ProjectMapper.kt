package com.flowtask.app.data.mapper

import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.domain.model.Project

fun ProjectEntity.toDomain(): Project = Project(
    id, name, description, groupName, startDate, endDate, colorId, icon, createdAt,
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id, name.trim(), description.trim(), groupName, startDate, endDate, colorId, icon, createdAt,
)
