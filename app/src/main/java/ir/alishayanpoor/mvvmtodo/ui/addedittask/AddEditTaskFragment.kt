package ir.alishayanpoor.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.alishayanpoor.mvvmtodo.R
import ir.alishayanpoor.mvvmtodo.databinding.FragmentAddEditTaskBinding
import ir.alishayanpoor.mvvmtodo.ui.tasks.TasksFragment
import ir.alishayanpoor.mvvmtodo.util.exhaustive
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            txtTaskName.setText(viewModel.taskName)
            txtTaskDesc.setText(viewModel.taskDescription)
            cbImportant.isChecked = viewModel.taskImportance
            cbImportant.jumpDrawablesToCurrentState()
            tvDateCreatedTitle.isVisible = viewModel.task != null
            tvDateCreatedValue.isVisible = viewModel.task != null
            tvDateCreatedValue.text = viewModel.task?.createdDateFormatted ?: ""
            txtTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }
            txtTaskDesc.addTextChangedListener {
                viewModel.taskDescription = it.toString()
            }
            cbImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }
            fabTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigationBackWithResult -> {
                        binding.txtTaskName.clearFocus()
                        binding.txtTaskDesc.clearFocus()
                        setFragmentResult(
                            TasksFragment.REQUEST_ADD_EDIT,
                            bundleOf(TasksFragment.RESULT_ADD_EDIT to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }
}