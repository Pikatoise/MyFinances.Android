package com.example.myfinances.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.myfinances.R

class DialogInfo(message: String) : DialogFragment() {
	private var _message: String = message

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.let {
			val builder = AlertDialog.Builder(it)
			builder.setTitle("Информация")
				.setMessage(_message)
				.setIcon(R.drawable.ic_info)
				.setPositiveButton("Ок") { _, _ ->
				}
			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}
}