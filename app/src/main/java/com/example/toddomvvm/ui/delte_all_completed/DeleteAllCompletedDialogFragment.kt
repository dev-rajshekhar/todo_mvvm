package com.example.toddomvvm.ui.delte_all_completed

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedDialogFragment : DialogFragment() {
    private val deleteAllViewModel: DeleteAllCompletedVm by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do You really want to delete?").setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") { _, _ ->
                deleteAllViewModel.onConfirmClick()
            }.create()
}