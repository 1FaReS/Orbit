package com.flowtask.app.di

import com.flowtask.app.data.repository.DataStoreUserPreferencesRepository
import com.flowtask.app.data.repository.OfflineFocusSessionRepository
import com.flowtask.app.data.repository.OfflineTagRepository
import com.flowtask.app.data.repository.OfflineProjectRepository
import com.flowtask.app.data.repository.OfflineRoutineRepository
import com.flowtask.app.data.repository.OfflineTaskRepository
import com.flowtask.app.data.reminder.WorkManagerTaskReminderScheduler
import com.flowtask.app.domain.repository.FocusSessionRepository
import com.flowtask.app.domain.repository.TagRepository
import com.flowtask.app.domain.repository.TaskRepository
import com.flowtask.app.domain.repository.UserPreferencesRepository
import com.flowtask.app.domain.repository.ProjectRepository
import com.flowtask.app.domain.repository.RoutineRepository
import com.flowtask.app.domain.reminder.TaskReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindTaskRepository(implementation: OfflineTaskRepository): TaskRepository
    @Binds abstract fun bindTagRepository(implementation: OfflineTagRepository): TagRepository
    @Binds abstract fun bindFocusRepository(
        implementation: OfflineFocusSessionRepository,
    ): FocusSessionRepository
    @Binds abstract fun bindUserPreferencesRepository(
        implementation: DataStoreUserPreferencesRepository,
    ): UserPreferencesRepository
    @Binds abstract fun bindProjectRepository(implementation: OfflineProjectRepository): ProjectRepository
    @Binds abstract fun bindRoutineRepository(implementation: OfflineRoutineRepository): RoutineRepository
    @Binds abstract fun bindTaskReminderScheduler(
        implementation: WorkManagerTaskReminderScheduler,
    ): TaskReminderScheduler
}
