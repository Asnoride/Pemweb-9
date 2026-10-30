package com.example.pemmob9.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pemmob9.R
import com.example.pemmob9.databinding.ItemTodoBinding
import com.example.pemmob9.model.Todo

/**
 * RecyclerView Adapter for displaying Todo items using CardView.
 */
class TodoAdapter(
    private val onItemClick: (Todo) -> Unit
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TodoViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TodoViewHolder(
        private val binding: ItemTodoBinding,
        private val onItemClick: (Todo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: Todo) {
            val context = binding.root.context
            binding.tvIdBadge.text = context.getString(R.string.task_id_label, todo.id)
            binding.tvTitle.text = todo.title
            binding.tvUserId.text = context.getString(R.string.user_id_label, todo.userId)

            if (todo.completed) {
                binding.tvStatus.text = context.getString(R.string.completed)
                binding.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_status_completed)
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text))
            } else {
                binding.tvStatus.text = context.getString(R.string.pending)
                binding.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_status_pending)
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
            }

            binding.cardView.setOnClickListener {
                onItemClick(todo)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }
}
