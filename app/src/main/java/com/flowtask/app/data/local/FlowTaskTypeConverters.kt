package com.flowtask.app.data.local

import androidx.room.TypeConverter
import com.flowtask.app.domain.model.EnergyLevel
import com.flowtask.app.domain.model.FocusSessionStatus
import com.flowtask.app.domain.model.Importance
import com.flowtask.app.domain.model.Priority
import com.flowtask.app.domain.model.RecurrenceFrequency
import com.flowtask.app.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class FlowTaskTypeConverters {
    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
    @TypeConverter fun dateToString(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun stringToDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
    @TypeConverter fun timeToString(value: LocalTime?): String? = value?.toString()
    @TypeConverter fun stringToTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
    @TypeConverter fun statusToString(value: TaskStatus): String = value.name
    @TypeConverter fun stringToStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
    @TypeConverter fun priorityToString(value: Priority): String = value.name
    @TypeConverter fun stringToPriority(value: String): Priority = Priority.valueOf(value)
    @TypeConverter fun energyToString(value: EnergyLevel): String = value.name
    @TypeConverter fun stringToEnergy(value: String): EnergyLevel = EnergyLevel.valueOf(value)
    @TypeConverter fun importanceToString(value: Importance): String = value.name
    @TypeConverter fun stringToImportance(value: String): Importance = Importance.valueOf(value)
    @TypeConverter fun recurrenceToString(value: RecurrenceFrequency?): String? = value?.name
    @TypeConverter fun stringToRecurrence(value: String?): RecurrenceFrequency? = value?.let(RecurrenceFrequency::valueOf)
    @TypeConverter fun focusStatusToString(value: FocusSessionStatus): String = value.name
    @TypeConverter fun stringToFocusStatus(value: String): FocusSessionStatus = FocusSessionStatus.valueOf(value)
    @TypeConverter fun intSetToString(value: Set<Int>): String = value.sorted().joinToString(",")
    @TypeConverter fun stringToIntSet(value: String): Set<Int> = value.takeIf(String::isNotBlank)
        ?.split(',')?.map(String::toInt)?.toSet().orEmpty()
}
