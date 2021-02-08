package com.example.toddomvvm.data

import androidx.room.*
import com.example.toddomvvm.ui.todo.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Query("SELECT * FROM TODO_TABLE WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' ||  :searchQuery || '%' ORDER BY important DESC, name")
    fun getTasksSortByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Todo>>

    @Query("SELECT * FROM TODO_TABLE WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' ||  :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Todo>>


    fun getTask(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Todo>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getTasksSortByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortByName(query, hideCompleted)

        }


    @Query("DELETE FROM todo_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()

}