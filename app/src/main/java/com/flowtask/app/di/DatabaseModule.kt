package com.flowtask.app.di

import android.content.Context
import androidx.room.Room
import com.flowtask.app.data.local.FlowTaskDatabase
import com.flowtask.app.data.local.dao.FocusSessionDao
import com.flowtask.app.data.local.dao.TagDao
import com.flowtask.app.data.local.dao.TaskDao
import com.flowtask.app.data.local.dao.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlowTaskDatabase =
        Room.databaseBuilder(context, FlowTaskDatabase::class.java, FlowTaskDatabase.DATABASE_NAME)
            .addMigrations(FlowTaskDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideTaskDao(database: FlowTaskDatabase): TaskDao = database.taskDao()
    @Provides fun provideTagDao(database: FlowTaskDatabase): TagDao = database.tagDao()
    @Provides fun provideFocusSessionDao(database: FlowTaskDatabase): FocusSessionDao = database.focusSessionDao()
    @Provides fun provideProjectDao(database: FlowTaskDatabase): ProjectDao = database.projectDao()
}
