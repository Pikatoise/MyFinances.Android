package com.example.myfinances.ui.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.R
import com.example.myfinances.Toasts
import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.operationType.OperationTypeResponse
import com.example.myfinances.api.repositories.ApiOperationTypeRepository
import com.example.myfinances.api.repositories.ApiPlanRepository
import com.example.myfinances.databinding.FragmentPlanBinding
import com.example.myfinances.db.AccessDataRepository
import com.example.myfinances.db.Plan
import com.example.myfinances.lists.PlanDateAdapter
import com.example.myfinances.lists.PlanDateData
import com.example.myfinances.lists.TypeAdapter
import com.example.myfinances.ui.dialogs.DialogInfo
import com.example.myfinances.ui.dialogs.DialogRemoveItem
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar


class PlanFragment : Fragment() {
	private lateinit var binding: FragmentPlanBinding

	private lateinit var accessDataRepo: AccessDataRepository
	private lateinit var apiPlanRepo: ApiPlanRepository
	private lateinit var apiTypesRepo: ApiOperationTypeRepository

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
		apiTypesRepo = ApiOperationTypeRepository(accessToken)

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
			showAddDialog()
		}

		fillAllOperations()

		return binding.root
	}

	private fun calendarDateToString(year: Int, month: Int, day: Int): String{
		val yearStr = "$year"

		val monthStr = if (month < 10) "0${month}" else "$month"

		val dayStr = if (day < 10) "0$day" else "$day"

		return "$yearStr-$monthStr-$dayStr"
	}

	fun ImageView.loadSvg(url: String) {
		val imageLoader = ImageLoader.Builder(this.context)
			.componentRegistry { add(SvgDecoder(this@loadSvg.context)) }
			.build()

		val request = ImageRequest.Builder(this.context)
			.crossfade(true)
			.crossfade(500)
			.data(url)
			.target(this)
			.build()

		imageLoader.enqueue(request)
	}

	@RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun showAddDialog(){
		val dialog = Dialog(this.requireContext())
		dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setCancelable(false)
		dialog.setContentView(R.layout.dialog_create_plan)

		val ivExit = dialog.findViewById<ImageView>(R.id.iv_dialog_create_plan_exit)
		val cbDate = dialog.findViewById<CheckBox>(R.id.cb_dialog_create_plan)
		val mcvIcon = dialog.findViewById<MaterialCardView>(R.id.mcv_dialog_create_plan_icon)
		val ivIcon = dialog.findViewById<ImageView>(R.id.iv_dialog_create_plan)
		val tvPreview = dialog.findViewById<TextView>(R.id.tv_dialog_create_plan_preview)
		val etTitle = dialog.findViewById<EditText>(R.id.et_dialog_create_plan_title)
		val tvDate = dialog.findViewById<TextView>(R.id.tv_dialog_create_plan_date)
		val rlSave = dialog.findViewById<RelativeLayout>(R.id.rl_dialog_create_plan_save)

		var selectedType: OperationTypeResponse? = null
		val calendar = Calendar.getInstance()

		ivExit.setOnClickListener {
			dialog.dismiss()
		}

		cbDate.isChecked = false
		tvDate.isClickable = false
		tvDate.setTextColor(getColor(this.requireContext(), R.color.gray_light))
		tvDate.text = calendarDateToString(
			calendar[Calendar.YEAR],
			calendar[Calendar.MONTH] + 1,
			calendar[Calendar.DAY_OF_MONTH])

		cbDate.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked){
				tvDate.isClickable = true
				tvDate.setTextColor(getColor(this.requireContext(), R.color.black))

				tvDate.setOnClickListener {
					DatePickerDialog(
						this.requireContext(),
						{ _, year, monthOfYear, dayOfMonth ->
							calendar[year, monthOfYear] = dayOfMonth

							tvDate.text = calendarDateToString(
								calendar[Calendar.YEAR],
								calendar[Calendar.MONTH] + 1,
								calendar[Calendar.DAY_OF_MONTH])
						}, calendar[Calendar.YEAR],
						calendar[Calendar.MONTH],
						calendar[Calendar.DAY_OF_MONTH]
					).show()
				}
			}
			else{
				tvDate.isClickable = false
				tvDate.setTextColor(getColor(this.requireContext(), R.color.gray_light))
			}
		}

		dialog.show()

		// Загрузка категорий и установка возможности выбора
		val requestTypes = CoroutineScope(Dispatchers.Main).async {
			apiTypesRepo.sendAllTypesRequest().await()
		}
		requestTypes.invokeOnCompletion {
			val responseTypes = runBlocking { requestTypes.await() }

			if (responseTypes.isSuccessful){
				val types = responseTypes.success!!.data

				mcvIcon.setOnClickListener {
					val dialogType = Dialog(this.requireContext())
					dialogType.window!!.setBackgroundDrawableResource(android.R.color.transparent)
					dialogType.requestWindowFeature(Window.FEATURE_NO_TITLE)
					dialogType.setCancelable(false)
					dialogType.setContentView(R.layout.dialog_select_type)

					val ivExitType = dialogType.findViewById<ImageView>(R.id.iv_dialog_select_type_exit)
					val lvTypes = dialogType.findViewById<ListView>(R.id.lv_dialog_select_type)

					ivExitType.setOnClickListener {
						dialogType.dismiss()
					}

					lvTypes.adapter = TypeAdapter(this.requireContext(),types.toList()) { position ->
						selectedType = types[position]

						tvPreview.visibility = INVISIBLE

						val iconPath = ApiClient.SERVER_URL_IMAGES + selectedType!!.iconSrc
						ivIcon.loadSvg(iconPath)

						dialogType.dismiss()
					}

					dialogType.show()
				}
			}
			else
				dialog.dismiss()
		}

		rlSave.setOnClickListener {
			var title = etTitle.text.toString().trim()

			var date = if (cbDate.isChecked)
				calendarDateToString(
					calendar[Calendar.YEAR],
					calendar[Calendar.MONTH] + 1,
					calendar[Calendar.DAY_OF_MONTH])
			else
				"Без срока"

			if (title.isEmpty()){
				Toasts.titleEmpty(this@PlanFragment.requireContext()).show()
				return@setOnClickListener
			}

			if (title.length > 30){
				Toasts.titleLong(this@PlanFragment.requireContext()).show()
				return@setOnClickListener
			}

			if (selectedType == null){
				Toasts.categoryEmpty(this@PlanFragment.requireContext()).show()
				return@setOnClickListener
			}

			val requestAdd = CoroutineScope(Dispatchers.Main).async {
				apiPlanRepo.sendAddPlanRequest(accessDataRepo.getUserId(), title, date, selectedType!!.id).await()
			}

			requestAdd.invokeOnCompletion {
				val responseAdd = runBlocking { requestAdd.await() }

				if (responseAdd.isSuccessful){
					fillAllOperations()
				}

				dialog.dismiss()
			}
		}
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

				if (plans.isNotEmpty()){
					binding.tvPlanEmpty.visibility = INVISIBLE
					binding.lvPlans.visibility = VISIBLE
				}
				else{
					binding.tvPlanEmpty.visibility = VISIBLE
					binding.lvPlans.visibility = INVISIBLE
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

				dates.sortWith(nullsFirst(compareBy {
					it.dayOfMonth * 24 + it.monthValue * 720 + it.year * 8640
				}))

				dates.forEach { date ->
					val dateStr =
						if (date == null)
							"Без срока"
						else{
							calendarDateToString(date.year, date.monthValue, date.dayOfMonth)
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