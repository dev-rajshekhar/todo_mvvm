package com.example.toddomvvm.ui.todo

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.toddomvvm.data.PreferenceManager
import com.example.toddomvvm.data.TaskDao
import com.example.toddomvvm.data.Todo
import com.example.toddomvvm.ui.ADDED_TASK_RESULT_OK
import com.example.toddomvvm.ui.ADD_TASK_RESULT_OK
import com.example.toddomvvm.utility.exhaustive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TodoVm @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager,
    @Assisted state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData<String>("searchQuery", "")


    val preferenceFlow = preferenceManager.preferenceFlow
    private val todoEventChannel = Channel<TodoEvent>()
    val todoEvent = todoEventChannel.receiveAsFlow()

    private val taskFlow =
        combine(searchQuery.asFlow(), preferenceFlow) { query, filterPreference ->
            Pair(query, filterPreference)
        }
            .flatMapLatest { (query, filterPreference) ->
                taskDao.getTask(query, filterPreference.sortOrder, filterPreference.hideCompleted)
            }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }


    fun onHideCompleted(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }

    val tasks = taskFlow.asLiveData()

    fun onTaskSelected(todo: Todo) {
        viewModelScope.launch {
            todoEventChannel.send(TodoEvent.NavigateToEditTask(todo))

        }
    }

    fun onTaskCheckChanged(checked: Boolean, todo: Todo) = viewModelScope.launch {
        taskDao.update(todo.copy(completed = checked))
    }

    fun onTaskSwiped(todo: Todo) = viewModelScope.launch {
        taskDao.delete(todo)
        todoEventChannel.send(TodoEvent.ShowUndoDeleteTodoMsg(todo))
    }

    fun onUndoDeleteClick(todo: Todo) = viewModelScope.launch {
        taskDao.insert(todo)
    }

    fun onAddNewTodoClicked() = viewModelScope.launch {
        todoEventChannel.send(TodoEvent.NavigateToAddTodoScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADDED_TASK_RESULT_OK -> showTaskEventMsg("Updated Successfully.")

            ADD_TASK_RESULT_OK -> showTaskEventMsg("Added Successfully.")


        }
    }

   private fun showTaskEventMsg(msg: String) = viewModelScope.launch {
        todoEventChannel.send(TodoEvent.ShowTaskSaveConfirmationMsg(msg))

    }

    fun onAllDeleteCompletedClick()=viewModelScope.launch {
        todoEventChannel.send(TodoEvent.DeleteAllCompleted)
    }

    sealed class TodoEvent {
        data class ShowUndoDeleteTodoMsg(val todo: Todo) : TodoEvent()
        data class ShowTaskSaveConfirmationMsg(val msg: String) : TodoEvent()
        object NavigateToAddTodoScreen : TodoEvent()
        object DeleteAllCompleted : TodoEvent()
        data class NavigateToEditTask(val todo: Todo) : TodoEvent()

    }

}


enum class SortOrder { BY_NAME, BY_DATE }