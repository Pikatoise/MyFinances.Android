package com.example.myfinances

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.myfinances.databinding.ActivityPlanAddBinding

class PlanAddActivity : AppCompatActivity() {
	private lateinit var binding: ActivityPlanAddBinding
	private lateinit var db: PlanRepository
	private var isUserInteracting: Boolean = false
	val imageList = intArrayOf(
		R.drawable.ic_alcohol,
		R.drawable.ic_products,
		R.drawable.ic_taxi,
		R.drawable.ic_bank,
		R.drawable.ic_clothes,
		R.drawable.ic_fun,
		R.drawable.ic_gift,
		R.drawable.ic_house,
		R.drawable.ic_medical,
		R.drawable.ic_salary,
		R.drawable.ic_study,
		R.drawable.ic_cafe
	)

	@SuppressLint("ClickableViewAccessibility")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityPlanAddBinding.inflate(layoutInflater)

		db = PlanRepository(this)

		setContentView(binding.root)

		var mSpinnerAdapter: SpinnerAdapter =
			SpinnerAdapter(this, imageList)

		binding.apply {
			ivBackBtn.setOnClickListener {
				finish()
			}

			spinnerType.adapter = mSpinnerAdapter

			spinnerType.setOnTouchListener(View.OnTouchListener { _, _ ->
				etName.hideKeyboard()

				false
			})

			cbPlanAddDate.setOnClickListener {
				if (cbPlanAddDate.isChecked){
					dpPlan.visibility = View.INVISIBLE
				}
				else
					dpPlan.visibility = View.VISIBLE
			}

			ivSaveBtn.setOnClickListener {
				val name = etName.text.toString()

				if (name.isEmpty() || name.length > 20){
					Toast.makeText(this@PlanAddActivity,"Некорректное название",Toast.LENGTH_SHORT).show()

					return@setOnClickListener
				}

				val day =
					if (dpPlan.dayOfMonth < 10)
						"0${dpPlan.dayOfMonth}"
					else
						dpPlan.dayOfMonth
				val month =
					if (dpPlan.month + 1 < 10)
						"0${dpPlan.month + 1}"
					else
						"${dpPlan.month + 1}"

				val date = if (cbPlanAddDate.isChecked) "Без срока" else "${dpPlan.year}-${month}-${day}"
				val type = spinnerType.selectedItemPosition
				val status = false

				var plan = Plan(-1,name,date,type,status)
				db.addPlan(plan)

				Toast.makeText(this@PlanAddActivity,"Успешно",Toast.LENGTH_SHORT).show()

				finish()
			}
		}

	}

	override fun onUserInteraction() {
		super.onUserInteraction()
		isUserInteracting = true
	}

	fun View.hideKeyboard() {
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(windowToken, 0)
	}
}