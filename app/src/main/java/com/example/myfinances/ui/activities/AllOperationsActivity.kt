package com.example.myfinances.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.myfinances.lists.DetailedListAdapter
import com.example.myfinances.lists.DetailedListData
import com.example.myfinances.db.Operation
import com.example.myfinances.db.Period
import com.example.myfinances.R
import com.example.myfinances.databinding.ActivityAllOperationsBinding

class AllOperationsActivity : AppCompatActivity() {
	private lateinit var binding: ActivityAllOperationsBinding
	private lateinit var periods: ArrayList<Period>
	private lateinit var operations: ArrayList<Operation>
	private val imageList = intArrayOf(
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAllOperationsBinding.inflate(layoutInflater)

		setContentView(binding.root)

		binding.ivAllOperationsBackBtn.setOnClickListener{
			finish()
		}

		val intent = this.intent

		if (intent != null){
			operations = intent.getParcelableArrayListExtra<Operation>("operations")!!
			periods = intent.getParcelableArrayListExtra<Period>("periods")!!

			fillAllOperations()
		}
	}

	private fun fillAllOperations(){
		val detailedDataArrayList = ArrayList<DetailedListData?>()

		if (operations.isEmpty()){
			binding.tvEmptyOperations.visibility = View.VISIBLE

			return
		}

		binding.tvEmptyOperations.visibility = View.INVISIBLE

		for (operation in operations) {
			val period = periods.first { period: Period -> period.id == operation.periodId  }

			val detailedListData = DetailedListData(
				imageList[operation.type],
				operation.title,
				operation.amount,
				period.year,
				period.month
			)
			detailedDataArrayList.add(detailedListData)
		}

		val listAdapter = DetailedListAdapter(this@AllOperationsActivity, detailedDataArrayList)
		binding.lvAllOperations.adapter = listAdapter
	}
}