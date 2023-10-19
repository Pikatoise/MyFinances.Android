package com.example.myfinances

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myfinances.databinding.ActivityAllOperationsBinding
import java.util.Calendar

class AllOperationsActivity : AppCompatActivity() {
	private lateinit var binding: ActivityAllOperationsBinding
	private lateinit var db: DbRepository
	private lateinit var imageList: IntArray
	private lateinit var operationsList: ArrayList<Operation>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAllOperationsBinding.inflate(layoutInflater)

		setContentView(binding.root)

		db = intent.getSerializableExtra("db") as DbRepository
		imageList = intent.getIntArrayExtra("images") as IntArray

		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val currentMonth = Calendar.getInstance().get(Calendar.MONTH+1)

		fillMonthOperations(currentYear	,currentMonth)

		binding.lvOperationsMonth.isClickable = true

		binding.lvOperationsMonth.setOnItemClickListener { _, _, position, _ ->
			val dialogRemove = DialogRemoveOperation {
				db.removeOperation(operationsList[position].id)

				fillMonthOperations(currentYear, currentMonth)

				Toast.makeText(this,"Готово", Toast.LENGTH_SHORT).show()
			}
			val manager = supportFragmentManager
			dialogRemove.show(manager,"removeDialog")
		}
	}

	private fun fillMonthOperations(year: Int, month: Int){
		var dataArrayList = ArrayList<ListData?>()
		binding.lvOperationsMonth.adapter = null

		operationsList = db.getPeriodOperations(db.getCurrentPeriod().id)

		if (operationsList.isEmpty()){
			binding.tvEmptyOperations.visibility = View.VISIBLE

			return
		}

		binding.tvEmptyOperations.visibility = View.INVISIBLE

		for (operation in operationsList) {
			dataArrayList.add(ListData( imageList[operation.type], operation.title, operation.amount ))
		}

		var listAdapter = ListAdapter(this@AllOperationsActivity, dataArrayList)
		binding.lvOperationsMonth.adapter = listAdapter
	}
}