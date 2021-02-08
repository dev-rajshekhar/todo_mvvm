package com.example.toddomvvm.ui.add_edit_todo

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
import com.example.toddomvvm.R
import com.example.toddomvvm.databinding.FragmentAddEditTaskBinding
import com.example.toddomvvm.utility.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTodoFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditVm by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            taskNameEt.setText(viewModel.taskName)
            importantCb.isChecked = viewModel.taskImportant
            importantCb.jumpDrawablesToCurrentState()
            taskDateCreatedTv.isVisible = viewModel.todo != null

            taskDateCreatedTv.text = "Created ${viewModel.todo?.createDateFormat}"

            taskNameEt.addTextChangedListener {
                viewModel.taskName = it.toString()
            }
            importantCb.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportant = isChecked
            }
            saveBtn.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->

                when (event) {
                    is AddEditVm.AddEditTaskEvent.ShowInvalidMsg -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditVm.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.taskNameEt.clearFocus()
                        setFragmentResult(
                            "add_edit_request", bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive

            }
        }

    }
}