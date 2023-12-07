package com.example.myfinances

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DialogInfo constructor(message: String) : DialogFragment() {
	private lateinit var _message: String

	init {
		_message = message
	}

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