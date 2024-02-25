package com.driveu.app.ui.framents

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.driveu.app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class CancelTrackingDialog : DialogFragment() {

    private var listener: (() -> Unit)? = null


    fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run")
            .setMessage("Are you sure to cancel the current run and delete all the data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                listener?.let {
                    it()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}