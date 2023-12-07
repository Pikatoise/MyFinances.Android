package com.example.myfinances

import android.content.Intent
import android.opengl.Visibility
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

		binding.buttonPlanClear.setOnClickListener {
			val dialogRemove = DialogRemoveItem("Очистить историю?") {
				db.removeAllPlans()

				fillAllOperations(binding)

				Toast.makeText(activity,"Готово", Toast.LENGTH_SHORT).show()
			}
			val manager = parentFragmentManager
			dialogRemove.show(manager,"removeDialog")
		}

		binding.rlPlanInfo.setOnClickListener {
			val dialogInfo = DialogInfo(
				"Стремительный ритм жизни и обилие дел позволяет легко забывать о " +
				"планируемых покупках. Использование планировщика расходов поможет " +
				"вам организовать свои расходы и не забыть о покупке важных вещей " +
				"или услуг в будущем." +
				"\n\n	- Добавляйте планируемые покупки на определенные даты" +
				"\n\n	- Оставляйте необходимые расходы без срока, чтобы не забыть о них в будущем" +
				"\n\n	- Отмечайте уже завершенные планы" +
				"\n\n* Очистка истории полностью удалит все задачи")
			val manager = parentFragmentManager
			dialogInfo.show(manager,"infoDialog")
		}

		binding.buttonPlanPlus.setOnClickListener {
			val intent = Intent(activity,PlanAddActivity::class.java)

			startActivity(intent)
		}

		return binding.root
	}

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onResume()
	{
		fillAllOperations(binding)

		super.onResume()
	}
	@RequiresApi(Build.VERSION_CODES.O)
	private fun fillAllOperations(binding: FragmentPlanBinding){
		val plansDateData: ArrayList<PlanDateData?> = arrayListOf()
		val dates = ArrayList<LocalDate?>()
		val plans = db.getAllPlans()

		if (plans.size > 0){
			binding.tvPlanEmpty.visibility = View.INVISIBLE
			binding.lvPlans.visibility = View.VISIBLE
		}
		else{
			binding.tvPlanEmpty.visibility = View.VISIBLE
			binding.lvPlans.visibility = View.INVISIBLE
			return
		}

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

		Toast.makeText(activity,"Обновлено",Toast.LENGTH_SHORT).show()
	}
}