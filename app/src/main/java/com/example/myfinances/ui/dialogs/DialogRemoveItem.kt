package com.example.myfinances.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.myfinances.R

class DialogRemoveItem(message: String, callBackSuccess: () -> Unit) : DialogFragment() {
	private var _callBack: () -> Unit = callBackSuccess
	private var _message: String = message

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