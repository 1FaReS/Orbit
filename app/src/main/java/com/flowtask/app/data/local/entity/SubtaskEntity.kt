package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentTaskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentTaskId"), Index(value = ["parentTaskId", "position"], unique = true)],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentTaskId: Long,
    val title: String,
    val isCompleted: Boolean,
    val position: Int,
)
