package com.example.myfinances.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.api.repositories.ApiCurrencyRepository
import com.example.myfinances.api.repositories.ApiOperationRepository
import com.example.myfinances.api.repositories.ApiOperationTypeRepository
import com.example.myfinances.api.repositories.ApiPeriodRepository
import com.example.myfinances.lists.SpinnerAdapter
import com.example.myfinances.databinding.ActivityOperationBinding
import com.example.myfinances.db.AccessDataRepository


class OperationActivity : AppCompatActivity() {
	private lateinit var binding: ActivityOperationBinding

	private lateinit var accessDataRepo: AccessDataRepository
	private lateinit var apiPeriodRepo: ApiPeriodRepository
	private lateinit var apiOperationRepo: ApiOperationRepository
	private lateinit var apiTypesRepo: ApiOperationTypeRepository
	private var operation = 0
	private var isUserInteracting: Boolean = false

	@SuppressLint("ClickableViewAccessibility")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityOperationBinding.inflate(layoutInflater)

		val sharedPreferencesContext = getSharedPreferences(AccessDataRepository.preferencesName, MODE_PRIVATE)
		accessDataRepo = AccessDataRepository(sharedPreferencesContext)

		val accessToken = accessDataRepo.getAccessToken()!!
		apiPeriodRepo = ApiPeriodRepository(accessToken)
		apiOperationRepo = ApiOperationRepository(accessToken)
		apiTypesRepo = ApiOperationTypeRepository(accessToken)

		binding.ivBackBtn.setOnClickListener {
			val intent = Intent()
			setResult(Activity.RESULT_CANCELED, intent)
			finish()
		}

		val intent = this.intent

//		if (intent != null){
//			operation = intent.getIntExtra("operation", 0)
//
//			var mSpinnerAdapter: SpinnerAdapter =
//				SpinnerAdapter(this, images)
//
//			binding.spinnerType.adapter = mSpinnerAdapter
//
//			binding.spinnerType.setOnTouchListener(OnTouchListener { _, _ ->
//				binding.etTitle.hideKeyboard()
//				binding.etAmount.hideKeyboard()
//
//				false
//			})
//
//			binding.buttonAdd.setOnClickListener {
//				if (checkFilled())
//					sendData()
//			}
//
//			if (operation == 1)
//				binding.tvOperation.text = "Доход"
//			else
//				binding.tvOperation.text = "Расход"
//		}

		setContentView(binding.root)
	}

	fun View.hideKeyboard() {
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(windowToken, 0)
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