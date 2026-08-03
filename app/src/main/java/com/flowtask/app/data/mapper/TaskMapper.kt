package com.flowtask.app.data.mapper

import com.flowtask.app.data.local.entity.SubtaskEntity
import com.flowtask.app.data.local.entity.TagEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.entity.TaskWithRelations
import com.flowtask.app.domain.model.RecurrenceRule
import com.flowtask.app.domain.model.ReminderSettings
import com.flowtask.app.domain.model.Subtask
import com.flowtask.app.domain.model.Tag
import com.flowtask.app.domain.model.Task

fun TaskWithRelations.toDomain(): Task = task.toDomain(
    tags = tags.map(TagEntity::toDomain),
    subtasks = subtasks.sortedBy(SubtaskEntity::position).map(SubtaskEntity::toDomain),
)

fun TaskEntity.toDomain(tags: List<Tag>, subtasks: List<Subtask>): Task = Task(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    dueDate = dueDate,
    dueTime = dueTime,
    estimatedDurationMinutes = estimatedDurationMinutes,
    actualDurationMinutes = actualDurationMinutes,
    energyLevel = energyLevel,
    importance = importance,
    createdAt = createdAt,
    completedAt = completedAt,
    recurrenceRule = recurrenceFrequency?.let { frequency ->
        RecurrenceRule(frequency, recurrenceInterval ?: 1, recurrenceDaysOfWeek, recurrenceEndDate)
    },
    category = category,
    tags = tags,
    subtasks = subtasks,
    reminder = reminderMinutesBefore?.let { ReminderSettings(reminderEnabled, it) },
    postponementCount = postponementCount,
    scheduledStart = scheduledStart,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title.trim(),
    description = description.trim(),
    status = status,
    priority = priority,
    dueDate = dueDate,
    dueTime = dueTime,
    estimatedDurationMinutes = estimatedDurationMinutes,
    actualDurationMinutes = actualDurationMinutes,
    energyLevel = energyLevel,
    importance = importance,
    createdAt = createdAt,
    completedAt = completedAt,
    recurrenceFrequency = recurrenceRule?.frequency,
    recurrenceInterval = recurrenceRule?.interval,
    recurrenceDaysOfWeek = recurrenceRule?.daysOfWeek.orEmpty(),
    recurrenceEndDate = recurrenceRule?.endDate,
    category = category,
    reminderEnabled = reminder?.enabled ?: false,
    reminderMinutesBefore = reminder?.minutesBefore,
    postponementCount = postponementCount,
    scheduledStart = scheduledStart,
)

fun Subtask.toEntity(taskId: Long): SubtaskEntity = SubtaskEntity(
    id = id,
    parentTaskId = taskId,
    title = title.trim(),
    isCompleted = isCompleted,
    position = position,
)

fun SubtaskEntity.toDomain(): Subtask = Subtask(id, parentTaskId, title, isCompleted, position)
fun Tag.toEntity(): TagEntity = TagEntity(id, name.trim(), icon, colorId)
fun TagEntity.toDomain(): Tag = Tag(id, name, icon, colorId)
