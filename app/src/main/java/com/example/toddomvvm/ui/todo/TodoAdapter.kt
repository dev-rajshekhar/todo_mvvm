package com.example.toddomvvm.ui.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.toddomvvm.data.Todo
import com.example.toddomvvm.databinding.ItemTodoBinding

class TodoAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Todo, TodoAdapter.TodoViewHolder>(ItemCallback()) {

    inner class TodoViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val todo = getItem(position)
                        listener.onItemClick(todo)
                    }
                }
                doneCheckBox.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val todo = getItem(position)
                        listener.onItemChecked(doneCheckBox.isChecked,todo)
                    }
                }
            }
        }

        fun bind(todo: Todo) {
            binding.apply {
                doneCheckBox.isChecked = todo.completed
                taskNameTv.text = todo.name
                taskNameTv.paint.isStrikeThruText = todo.completed
                taskDoneImg.isVisible = todo.important
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    interface OnItemClickListener {
        fun onItemClick(todo: Todo)
        fun onItemChecked(checked: Boolean, todo: Todo)
    }

    class ItemCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean =
            oldItem == newItem

    }
}