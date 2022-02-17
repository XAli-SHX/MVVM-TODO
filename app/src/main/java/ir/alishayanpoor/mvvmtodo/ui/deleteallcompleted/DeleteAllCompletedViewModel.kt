package ir.alishayanpoor.mvvmtodo.ui.deleteallcompleted

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.alishayanpoor.mvvmtodo.data.TaskDao
import ir.alishayanpoor.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    fun onConfirmDelete() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}