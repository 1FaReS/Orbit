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
import com.flowtask.app.data.local.entity.FocusSessionEntity
import com.flowtask.app.data.local.entity.SubtaskEntity
import com.flowtask.app.data.local.entity.TagEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.entity.TaskTagCrossRef
import com.flowtask.app.data.local.entity.ProjectEntity

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        FocusSessionEntity::class,
        ProjectEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(FlowTaskTypeConverters::class)
abstract class FlowTaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun projectDao(): ProjectDao

    companion object {
        const val DATABASE_NAME = "flowtask.db"

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
    }
}
