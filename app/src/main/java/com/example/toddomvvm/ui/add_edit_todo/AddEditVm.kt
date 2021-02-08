package com.example.toddomvvm.ui.add_edit_todo

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toddomvvm.data.TaskDao
import com.example.toddomvvm.data.Todo
import com.example.toddomvvm.ui.ADDED_TASK_RESULT_OK
import com.example.toddomvvm.ui.ADD_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditVm @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val taskDao: TaskDao
) : ViewModel() {
    val todo = state.get<Todo>("todo")
    private fun showInvalidInputMsg(msg: String) = viewModelScope.launch {
        addEditTaskChannel.send(AddEditTaskEvent.ShowInvalidMsg(msg))
    }

    private val addEditTaskChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskChannel.receiveAsFlow()


    private fun upDateTask(updatedTask: Todo) = viewModelScope.launch {
        taskDao.insert(updatedTask)
        addEditTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADDED_TASK_RESULT_OK))

    }

    private fun createTask(newTask: Todo) = viewModelScope.launch {
        taskDao.insert(newTask)
        addEditTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))

    }

    var taskName = state.get<String>("taskName") ?: todo?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportant = state.get<Boolean>("taskImportant") ?: todo?.important ?: false
        set(value) {
            field = value
            state.set("taskImportant", value)
        }


    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMsg("Task name can not be empty.")
            return
        }
        if (todo != null) {
            val updatedTask = todo.copy(name = taskName, important = taskImportant)
            upDateTask(updatedTask)
        } else {
            val newTask = Todo(name = taskName, important = taskImportant)
            createTask(newTask)
        }

    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidMsg(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}