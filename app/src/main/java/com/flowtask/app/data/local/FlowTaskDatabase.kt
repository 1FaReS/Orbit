package com.flowtask.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flowtask.app.data.local.dao.FocusSessionDao
import com.flowtask.app.data.local.dao.TagDao
import com.flowtask.app.data.local.dao.TaskDao
import com.flowtask.app.data.local.dao.ProjectDao
import com.flowtask.app.data.local.dao.RoutineDao
import com.flowtask.app.data.local.entity.FocusSessionEntity
import com.flowtask.app.data.local.entity.SubtaskEntity
import com.flowtask.app.data.local.entity.TagEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.entity.TaskTagCrossRef
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.DaySummaryEntity
import com.flowtask.app.data.local.entity.RoutineEntity
import com.flowtask.app.data.local.entity.RoutineItemEntity

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        FocusSessionEntity::class,
        ProjectEntity::class,
        RoutineEntity::class,
        RoutineItemEntity::class,
        DaySummaryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(FlowTaskTypeConverters::class)
abstract class FlowTaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun projectDao(): ProjectDao
    abstract fun routineDao(): RoutineDao

    companion object {
        const val DATABASE_NAME = "orbit.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        groupName TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT,
                        colorId TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_name ON projects(name)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS routines (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        repeatDays TEXT NOT NULL,
                        reminderTime TEXT,
                        enabled INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routines_name ON routines(name)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS routine_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        routineId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        position INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_routine_items_routineId ON routine_items(routineId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS day_summaries (
                        date TEXT NOT NULL PRIMARY KEY,
                        completedTasks INTEGER NOT NULL,
                        totalTasks INTEGER NOT NULL,
                        focusedMinutes INTEGER NOT NULL,
                        reflection TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
