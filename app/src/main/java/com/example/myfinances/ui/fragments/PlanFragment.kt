package com.example.myfinances.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.example.myfinances.api.repositories.ApiPlanRepository
import com.example.myfinances.ui.dialogs.DialogInfo
import com.example.myfinances.ui.dialogs.DialogRemoveItem
import com.example.myfinances.db.Plan
import com.example.myfinances.ui.activities.PlanAddActivity
import com.example.myfinances.lists.PlanDateAdapter
import com.example.myfinances.lists.PlanDateData
import com.example.myfinances.databinding.FragmentPlanBinding
import com.example.myfinances.db.AccessDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.collections.ArrayList

class PlanFragment : Fragment() {
	private lateinit var binding: FragmentPlanBinding

	private lateinit var apiPlanRepo: ApiPlanRepository
	private lateinit var accessDataRepo: AccessDataRepository

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentPlanBinding.inflate(inflater,container,false)

		val sharedPreferencesContext = this@PlanFragment.requireContext().getSharedPreferences(AccessDataRepository.preferencesName, MODE_PRIVATE)
		accessDataRepo = AccessDataRepository(sharedPreferencesContext)

		val accessToken = accessDataRepo.getAccessToken()!!

		apiPlanRepo = ApiPlanRepository(accessToken)

		binding.rlPlanInfo.setOnClickListener {
			val dialogInfo = DialogInfo(
				"Стремительный ритм жизни и обилие дел позволяет легко забывать о " +
				"планируемых покупках. Использование планировщика расходов поможет " +
				"вам организовать свои расходы и не забыть о покупке важных вещей " +
				"или услуг в будущем." +
				"\n\n	- Добавляйте планируемые покупки на определенные даты" +
				"\n\n	- Оставляйте необходимые расходы без срока, чтобы не забыть о них в будущем" +
				"\n\n	- Отмечайте уже завершенные планы")
			val manager = parentFragmentManager
			dialogInfo.show(manager,"infoDialog")
		}

		binding.buttonPlanPlus.setOnClickListener {
			val intent = Intent(activity, PlanAddActivity::class.java)

			startActivity(intent)
		}

		fillAllOperations()

		return binding.root
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun fillAllOperations(){
		val request = CoroutineScope(Dispatchers.Main).async {
			apiPlanRepo.sendUserPlansRequest(accessDataRepo.getUserId()).await()
		}

		request.invokeOnCompletion {
			val response = runBlocking { request.await() }

			if (response.isSuccessful){
				val plans = response.success!!.data

				if (plans.size > 0){
					binding.tvPlanEmpty.visibility = View.INVISIBLE
				}
				else{
					binding.tvPlanEmpty.visibility = View.VISIBLE
					return@invokeOnCompletion
				}

				val plansDateData: ArrayList<PlanDateData?> = arrayListOf()
				val dates = ArrayList<LocalDate?>()

				val planWithoutDate = plans.find { x -> x.finalDate == "Без срока" }

				if (planWithoutDate != null)
					dates.add(null)

				for (i in 0..plans.size-1){
					if (plans[i].finalDate == "Без срока")
						continue

					val date = LocalDate.parse(plans[i].finalDate)

					if (!dates.contains(date))
						dates.add(date)
				}

				dates.sortWith(nullsFirst(compareByDescending {
					it.dayOfMonth * 24 + it.monthValue * 720 + it.year * 8640
				}))

				dates.forEach { date ->
					val dateStr =
						if (date == null)
							"Без срока"
						else{
							val year = "${date.year}"

							val month =
								if (date.monthValue < 10)
									"0${date.monthValue}"
								else
									"${date.monthValue}"

							val day =
								if (date.dayOfMonth < 10)
									"0${date.dayOfMonth}"
								else
									date.dayOfMonth

							"${year}-${month}-${day}"
						}
					val currentDatePlans: ArrayList<Plan?> = arrayListOf()

					plans.forEach{
						if (it.finalDate == dateStr)
							currentDatePlans.add(Plan(it.id, it.name, it.finalDate, it.typeIconSrc, it.status == 1))
					}

					plansDateData.add(PlanDateData(dateStr,currentDatePlans))
				}

				val listAdapter = PlanDateAdapter(
					this.requireContext(),
					plansDateData,
					{ status, planId ->
						CoroutineScope(Dispatchers.Main).async {
							apiPlanRepo.sendChangeStatusRequest(planId, if (status) 1 else 0).await()
						}
					},
					{ planId ->
						val dialogRemove = DialogRemoveItem("Удалить планируемый расход?") {
							val requestDelete = CoroutineScope(Dispatchers.Main).async {
								apiPlanRepo.sendDeletePlanRequest(planId).await()
							}

							requestDelete.invokeOnCompletion {
								val responseDelete = runBlocking { requestDelete.await() }

								if (responseDelete.isSuccessful){
									fillAllOperations()

									Toast.makeText(activity,"Готово", Toast.LENGTH_SHORT).show()
								}
								else
									Toast.makeText(activity,"Ошибка", Toast.LENGTH_SHORT).show()
							}
						}
						val manager = parentFragmentManager
						dialogRemove.show(manager,"removeDialog")
					}
				)
				binding.lvPlans.adapter = listAdapter
			}
		}
	}
}