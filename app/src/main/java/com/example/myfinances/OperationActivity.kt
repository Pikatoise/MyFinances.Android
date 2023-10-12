package com.example.myfinances

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.databinding.ActivityOperationBinding


class OperationActivity : AppCompatActivity() {
	private lateinit var binding: ActivityOperationBinding
	private var operation = 0

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
			val images = intent.getIntArrayExtra("images")

			var mSpinnerAdapter: SpinnerAdapter = SpinnerAdapter(this,images)

			binding.spinnerType.adapter = mSpinnerAdapter

//			binding.spinnerType.onItemSelectedListener =
//				object : AdapterView.OnItemSelectedListener {
//					override O
//
//					override fun onItemSelected(
//						adapterView: AdapterView<*>?,
//						view: View,
//						i: Int,
//						l: Long
//					) {
//						if (isUserInteracting) {
//							Toast.makeText(
//								this@MainActivity,
//								spinnerTitles.get(i),
//								Toast.LENGTH_SHORT
//							).show()
//						}
//					}
//
//					override fun onNothingSelected(adapterView: AdapterView<*>?) {}
//				}

			if (operation == 1)
				binding.tvOperation.text = "Доход"
			else
				binding.tvOperation.text = "Расход"

		}
	}
}