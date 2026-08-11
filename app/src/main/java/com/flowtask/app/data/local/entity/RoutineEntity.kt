package com.flowtask.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "routines", indices = [Index("name", unique = true)])
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val repeatDays: Set<DayOfWeek>,
    val reminderTime: LocalTime?,
    val enabled: Boolean,
)

@Entity(tableName = "routine_items", indices = [Index("routineId")])
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val title: String,
    val position: Int,
)

data class RoutineWithItems(
    @Embedded val routine: RoutineEntity,
    @Relation(parentColumn = "id", entityColumn = "routineId")
    val items: List<RoutineItemEntity>,
)

@Entity(tableName = "day_summaries")
data class DaySummaryEntity(
    @PrimaryKey val date: LocalDate,
    val completedTasks: Int,
    val totalTasks: Int,
    val focusedMinutes: Int,
    val reflection: String,
)
