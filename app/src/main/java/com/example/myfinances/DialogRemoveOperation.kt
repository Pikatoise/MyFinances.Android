package com.example.myfinances

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class DialogRemoveOperation constructor(callBackSuccess: () -> Unit) : DialogFragment() {
	private lateinit var callBack: () -> Unit

	init {
		callBack = callBackSuccess
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.let {
			val builder = AlertDialog.Builder(it)
			builder.setTitle("Подтверждение")
				.setMessage("Удалить операцию?")
				.setIcon(R.drawable.ic_trash)
				.setCancelable(true)
				.setPositiveButton("Удалить") { _, _ ->
					callBack()
				}
				.setNegativeButton("Отмена") { _, _ ->
				}
			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}
}