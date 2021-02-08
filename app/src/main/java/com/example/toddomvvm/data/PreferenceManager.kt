package com.example.toddomvvm.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.example.toddomvvm.ui.todo.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferenceManager"

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore("user_preferences")
    val preferenceFlow = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error Reading Exception", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preference ->
        val sortOrder =
            SortOrder.valueOf(preference[PreferenceKey.SORT_ORDER] ?: SortOrder.BY_DATE.name)

        val hideCompleted = preference[PreferenceKey.HIDE_COMPLETED] ?: false
        FilterPreference(sortOrder, hideCompleted)
    }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit {pref->
            pref[PreferenceKey.SORT_ORDER]=sortOrder.name

        }}
        suspend fun updateHideCompleted(hideCompleted: Boolean){
            dataStore.edit {pref->
                pref[PreferenceKey.HIDE_COMPLETED]=hideCompleted

            }
    }
    private object PreferenceKey {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}

data class FilterPreference(val sortOrder: SortOrder, val hideCompleted: Boolean)