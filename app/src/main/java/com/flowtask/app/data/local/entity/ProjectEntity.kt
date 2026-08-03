package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "projects", indices = [Index(value = ["name"], unique = true)])
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val groupName: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val colorId: String,
    val icon: String,
    val createdAt: Instant,
)
