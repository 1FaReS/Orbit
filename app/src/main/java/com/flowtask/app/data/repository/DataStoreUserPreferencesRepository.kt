package com.flowtask.app.data.repository

import com.flowtask.app.data.local.preferences.FlowTaskPreferencesDataSource
import com.flowtask.app.domain.model.UserPreferences
import com.flowtask.app.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataSource: FlowTaskPreferencesDataSource,
) : UserPreferencesRepository {
    override val preferences: Flow<UserPreferences> = dataSource.preferences
    override suspend fun update(transform: (UserPreferences) -> UserPreferences) = dataSource.update(transform)
}
