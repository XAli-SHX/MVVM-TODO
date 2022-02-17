package ir.alishayanpoor.mvvmtodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.alishayanpoor.mvvmtodo.data.Task
import ir.alishayanpoor.mvvmtodo.databinding.ItemTaskBinding

class TasksAdapter(private val listener: OnItemClickedListener) :
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClicked(task)
                    }
                }
                cbComplete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckboxClicked(task, cbComplete.isChecked)
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                cbComplete.isChecked = task.completed
                tvName.text = task.name
                tvName.paint.isStrikeThruText = task.completed
                imgPriority.isVisible = task.important
            }
        }
    }

    interface OnItemClickedListener {
        fun onItemClicked(task: Task)
        fun onCheckboxClicked(task: Task, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}