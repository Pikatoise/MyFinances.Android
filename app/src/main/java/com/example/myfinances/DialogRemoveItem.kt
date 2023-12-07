package com.example.myfinances

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DialogRemoveItem constructor(message: String,callBackSuccess: () -> Unit) : DialogFragment() {
	private lateinit var _callBack: () -> Unit
	private lateinit var _message: String

	init {
		_callBack = callBackSuccess
		_message = message
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.let {
			val builder = AlertDialog.Builder(it)
			builder.setTitle("Подтверждение")
				.setMessage(_message)
				.setIcon(R.drawable.ic_trash)
				.setCancelable(true)
				.setPositiveButton("Удалить") { _, _ ->
					_callBack()
				}
				.setNegativeButton("Отмена") { _, _ ->
				}
			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}
}