package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowtask.app.domain.model.FocusSessionStatus
import java.time.Instant

@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("taskId"), Index("status"), Index("startedAt")],
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val durationMinutes: Int,
    val status: FocusSessionStatus,
)
