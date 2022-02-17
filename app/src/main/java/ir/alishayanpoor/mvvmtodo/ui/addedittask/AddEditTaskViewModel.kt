package ir.alishayanpoor.mvvmtodo.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.alishayanpoor.mvvmtodo.data.Task
import ir.alishayanpoor.mvvmtodo.data.TaskDao
import ir.alishayanpoor.mvvmtodo.ui.ADD_TASK_RESULT_OK
import ir.alishayanpoor.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel() {
    val task: Task? = state.get<Task>("task")
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    var taskDescription = state.get<String>("taskDescription") ?: task?.description ?: ""
        set(value) {
            field = value
            state.set("taskDescription", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if (task != null) {
            val updatedTask = task.copy(
                name = taskName,
                description = taskDescription,
                important = taskImportance
            )
            updatedTask(updatedTask)
        } else {
            val newTask =
                Task(name = taskName, description = taskDescription, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insert(newTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigationBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updatedTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigationBackWithResult(EDIT_TASK_RESULT_OK))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigationBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}