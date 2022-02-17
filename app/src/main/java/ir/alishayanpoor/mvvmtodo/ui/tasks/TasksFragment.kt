package ir.alishayanpoor.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.alishayanpoor.mvvmtodo.R
import ir.alishayanpoor.mvvmtodo.data.SortOrder
import ir.alishayanpoor.mvvmtodo.data.Task
import ir.alishayanpoor.mvvmtodo.databinding.FragmentTasksBinding
import ir.alishayanpoor.mvvmtodo.util.exhaustive
import ir.alishayanpoor.mvvmtodo.util.onQueryTextChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {
    companion object {
        const val REQUEST_ADD_EDIT = "add_edit_request"
        const val RESULT_ADD_EDIT = "add_edit_result"
    }

    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView: SearchView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        val tasksAdapter = TasksAdapter(object : TasksAdapter.OnItemClickedListener {
            override fun onItemClicked(task: Task) {
                viewModel.onTaskSelected(task)
            }

            override fun onCheckboxClicked(task: Task, isChecked: Boolean) {
                viewModel.onTaskCheckedChanged(task, isChecked)
            }

        })
        binding.apply {
            rvTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            if (tasksAdapter.currentList.isNullOrEmpty()) showNothing(this)
            else hideNothing(this)
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(rvTasks)

            fabTask.setOnClickListener {
                viewModel.onAddNewTaskClicked()
            }
        }

        setFragmentResultListener(REQUEST_ADD_EDIT) { _, bundle ->
            val result = bundle.getInt(RESULT_ADD_EDIT)
            viewModel.onAddEditResult(result)
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
            if (it.isNullOrEmpty()) {
                showNothing(binding)
            } else {
                hideNothing(binding)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClicked(event.task)
                            }.show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                task = null, title = "New task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                task = event.task, title = "Edit task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.text, Snackbar.LENGTH_LONG).show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =
                            TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    private fun hideNothing(binding: FragmentTasksBinding) {
        binding.apply {
            layNothing.visibility = View.GONE
            animationNothing.cancelAnimation()
        }
    }

    private fun showNothing(binding: FragmentTasksBinding) {
        binding.apply {
            layNothing.visibility = View.VISIBLE
            animationNothing.playAnimation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.apply {
                    isChecked = !isChecked
                    viewModel.onHideCompletedClicked(isChecked)
                }
                true
            }
            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)
    }
}