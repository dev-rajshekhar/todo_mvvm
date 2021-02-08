package com.example.toddomvvm.data

import androidx.room.CoroutinesRoom
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.toddomvvm.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Todo::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TaskDao
    class Callback @Inject constructor(
        private val todoDatabase: Provider<TodoDatabase>,
      @ApplicationScope  private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val doa = todoDatabase.get().todoDao()
            applicationScope.launch {
                doa.insert(Todo("Work Out"))
                doa.insert(Todo("Breakfast", important = true))
                doa.insert(Todo("Clean Room"))
                doa.insert(Todo("Fix Garden", completed = true))
                doa.insert(Todo("Laundry ", important = true))
                doa.insert(Todo("Call Friend"))
            }
        }
    }
}