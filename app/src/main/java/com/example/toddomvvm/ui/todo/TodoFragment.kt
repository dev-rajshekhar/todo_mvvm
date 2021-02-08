package com.example.toddomvvm.ui.todo

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
import com.example.toddomvvm.R
import com.example.toddomvvm.data.Todo
import com.example.toddomvvm.databinding.FragmentTaskBinding
import com.example.toddomvvm.utility.exhaustive
import com.example.toddomvvm.utility.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TodoFragment : Fragment(R.layout.fragment_task), TodoAdapter.OnItemClickListener {
    private val viewModel: TodoVm by viewModels()
    private lateinit var searchView: SearchView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataBinding = FragmentTaskBinding.bind(view)
        val todAdapter = TodoAdapter(this)
        dataBinding.apply {
            recyclerViewToDo.apply {
                adapter = todAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val todo = todAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(todo)
                }
            }).attachToRecyclerView(recyclerViewToDo)

            addBtn.setOnClickListener {

                viewModel.onAddNewTodoClicked()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)

        }
        viewModel.tasks.observe(viewLifecycleOwner) {
            todAdapter.submitList(it)
        }
        setHasOptionsMenu(true)


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.todoEvent.collect { event ->
                when (event) {
                    is TodoVm.TodoEvent.ShowUndoDeleteTodoMsg -> {
                        Snackbar.make(requireView(), "Task Delete", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeleteClick(event.todo)
                            }.show()
                    }
                    is TodoVm.TodoEvent.NavigateToAddTodoScreen -> {
                        val action = TodoFragmentDirections.actionTodoFragmentToAddEditTodoFragment(
                            null,
                            "New Task"
                        )
                        findNavController().navigate(action)

                    }
                    is TodoVm.TodoEvent.NavigateToEditTask -> {

                        val action = TodoFragmentDirections.actionTodoFragmentToAddEditTodoFragment(
                            event.todo,
                            title = "Edit Task"
                        )
                        findNavController().navigate(action)
                    }
                    is TodoVm.TodoEvent.ShowTaskSaveConfirmationMsg -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    is TodoVm.TodoEvent.DeleteAllCompleted -> {
                        val action =
                            TodoFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()

                        findNavController().navigate(action)

                    }
                }.exhaustive
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)
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
            menu.findItem(R.id.action_hide_task_completed).isChecked =
                viewModel.preferenceFlow.first().hideCompleted
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
            R.id.action_delete_all_completed -> {
                viewModel.onAllDeleteCompletedClick()
                true
            }
            R.id.action_hide_task_completed -> {
                item.isChecked = !item.isChecked

                viewModel.onHideCompleted(item.isChecked)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(todo: Todo) {
        viewModel.onTaskSelected(todo)

    }

    override fun onItemChecked(checked: Boolean, todo: Todo) {
        viewModel.onTaskCheckChanged(checked, todo)

    }

    override fun onDestroy() {
        super.onDestroy()
        searchView .setOnQueryTextListener(null)
    }
}