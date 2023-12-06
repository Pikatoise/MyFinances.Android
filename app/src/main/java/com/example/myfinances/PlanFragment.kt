package com.example.myfinances

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.myfinances.databinding.FragmentPlanBinding
import java.time.LocalDate
import kotlin.collections.ArrayList

class PlanFragment : Fragment() {
	private lateinit var binding: FragmentPlanBinding
	private lateinit var db: PlanRepository

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentPlanBinding.inflate(inflater,container,false)

		db = PlanRepository(this.requireContext())

		fillAllOperations(binding)

		return binding.root
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun fillAllOperations(binding: FragmentPlanBinding){
		val plansDateData: ArrayList<PlanDateData?> = arrayListOf()
		val plans = db.getAllPlans()
		val dates = ArrayList<LocalDate?>()

		for (i in 0..plans.size-1){
			if (plans[i].date == "Без срока"){
				dates.add(null)
				break
			}
		}

		for (i in 0..plans.size-1){
			if (plans[i].date == "Без срока")
				continue

			val date = LocalDate.parse(plans[i].date)

			if (!dates.contains(date))
				dates.add(date)
		}

		dates.sortWith(nullsFirst(compareByDescending {
			it.dayOfMonth * 24 + it.monthValue * 720 + it.year * 8640
		}))

		dates.forEach {
			val dateStr =
				if (it == null)
					"Без срока"
				else{
					val year = "${it.year}"
					val month = "${it.monthValue}"
					val day =
						if (it.dayOfMonth < 10)
							"0${it.dayOfMonth}"
						else
							it.dayOfMonth

					"${year}-${month}-${day}"
				}

			val currentDatePlans: ArrayList<Plan?>? = arrayListOf()

			plans.forEach{
				if (it.date == dateStr)
					currentDatePlans!!.add(it)
			}

			plansDateData.add(PlanDateData(dateStr,currentDatePlans))
		}

		val listAdapter = PlanDateAdapter(
			this.requireContext(),
			plansDateData,
			{ status, planId ->
				db.changePlanStatus(planId,status)
			},
			{ planId ->
				val dialogRemove = DialogRemoveItem("Удалить планируемый расход?") {
					db.removePlan(planId)

					fillAllOperations(binding)

					Toast.makeText(activity,"Готово", Toast.LENGTH_SHORT).show()
				}
				val manager = parentFragmentManager
				dialogRemove.show(manager,"removeDialog")
			}
		)
		binding.lvPlans.adapter = listAdapter
	}
}