package com.example.myfinances

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.databinding.ActivityOperationBinding


class OperationActivity : AppCompatActivity() {
	private lateinit var binding: ActivityOperationBinding
	private var operation = 0
	private var isUserInteracting: Boolean = false
	private var images: IntArray? = intArrayOf()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityOperationBinding.inflate(layoutInflater)

		setContentView(binding.root)

		binding.ivBackBtn.setOnClickListener {
			val intent = Intent()
			setResult(Activity.RESULT_CANCELED, intent)
			finish()
		}

		binding.buttonAdd.setOnClickListener {
			val intent = Intent()
			setResult(Activity.RESULT_OK, intent)
			finish()
		}

		val intent = this.intent

		if (intent != null){
			operation = intent.getIntExtra("operation", 0)
			images = intent.getIntArrayExtra("images")

			var mSpinnerAdapter: SpinnerAdapter = SpinnerAdapter(this,images)

			binding.spinnerType.adapter = mSpinnerAdapter

//			binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//				override fun onItemSelected(adapterView: AdapterView<*>?, view: View,i: Int,l: Long) {
//					if (isUserInteracting) {
//						Toast.makeText(this@OperationActivity, binding.spinnerType.selectedItemPosition.toString(), Toast.LENGTH_SHORT).show()
//					}
//				}
//
//				override fun onNothingSelected(adapterView: AdapterView<*>?) {
//
//				}
//			}

			binding.buttonAdd.setOnClickListener {
				if (checkFilled())
					sendData()
			}

			if (operation == 1)
				binding.tvOperation.text = "Доход"
			else
				binding.tvOperation.text = "Расход"
		}
	}

	fun sendData() {
		var title = binding.etTitle.text.toString()
		var amount = binding.etAmount.getDouble()
		var image = binding.spinnerType.selectedItemPosition

		if (operation == -1)
			amount *= -1

		val intent = Intent()

		intent.putExtra("title",title)
		intent.putExtra("amount",amount)
		intent.putExtra("image",image)

		setResult(Activity.RESULT_OK,intent)

		finish()
	}

	fun checkFilled(): Boolean{
		if (binding.etTitle.text.isNullOrEmpty()){
			Toast.makeText(this,"Введите описание",Toast.LENGTH_SHORT).show()
			return false
		}

		if (binding.etTitle.text.length > 60){
			Toast.makeText(this,"Описание слишком длинное",Toast.LENGTH_SHORT).show()
			return false
		}

		if (binding.etAmount.getDouble() == 0.0){
			Toast.makeText(this,"Введите сумму",Toast.LENGTH_SHORT).show()
			return false
		}

		if (binding.etAmount.getDouble() > 50000.0){
			Toast.makeText(this,"Сумма слишком большая\n   (лимит 50 тыс.)",Toast.LENGTH_SHORT).show()
			return false
		}

		return true
	}
	override fun onUserInteraction() {
		super.onUserInteraction()
		isUserInteracting = true
	}

	fun EditText.getDouble(): Double = try {
		text.toString().toDouble()
	} catch (e: NumberFormatException) {
		e.printStackTrace()
		0.0
	}
}