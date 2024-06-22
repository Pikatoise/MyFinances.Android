package com.example.myfinances.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat.getColor
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.ArrayResources
import com.example.myfinances.Currencies
import com.example.myfinances.ui.activities.AllOperationsActivity
import com.example.myfinances.ui.dialogs.DialogRemoveItem
import com.example.myfinances.lists.CurrentPeriodAdapter
import com.example.myfinances.lists.CurrentPeriodItem
import com.example.myfinances.NumberFormats
import com.example.myfinances.R
import com.example.myfinances.Toasts
import com.example.myfinances.api.models.operation.OperationResponse
import com.example.myfinances.api.models.operationType.OperationTypeResponse
import com.example.myfinances.api.repositories.ApiAuthRepository
import com.example.myfinances.api.repositories.ApiCurrencyRepository
import com.example.myfinances.api.repositories.ApiOperationRepository
import com.example.myfinances.api.repositories.ApiOperationTypeRepository
import com.example.myfinances.api.repositories.ApiPeriodRepository
import com.example.myfinances.api.repositories.ApiTokenRepository
import com.example.myfinances.databinding.FragmentMainBinding
import com.example.myfinances.db.AccessDataRepository
import com.example.myfinances.lists.TypeAdapter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

class MainFragment : Fragment() {
	private lateinit var binding: FragmentMainBinding

	private lateinit var apiAuthRepo: ApiAuthRepository
	private lateinit var apiTokenRepo: ApiTokenRepository
	private lateinit var accessDataRepo: AccessDataRepository
	private lateinit var apiCurrencyRepo: ApiCurrencyRepository
	private lateinit var apiPeriodRepo: ApiPeriodRepository
	private lateinit var apiOperationRepo: ApiOperationRepository
	private lateinit var apiTypesRepo: ApiOperationTypeRepository

	private var currentPeriodId: Int = -1
	private lateinit var currentItems: List<OperationResponse>

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentMainBinding.inflate(inflater,container,false)

		val sharedPreferencesContext = this@MainFragment.requireContext().getSharedPreferences(AccessDataRepository.preferencesName, MODE_PRIVATE)
		accessDataRepo = AccessDataRepository(sharedPreferencesContext)

		val accessToken = accessDataRepo.getAccessToken()!!

		apiAuthRepo = ApiAuthRepository()
		apiTokenRepo = ApiTokenRepository()
		apiPeriodRepo = ApiPeriodRepository(accessToken)
		apiCurrencyRepo = ApiCurrencyRepository(accessToken)
		apiOperationRepo = ApiOperationRepository(accessToken)
		apiTypesRepo = ApiOperationTypeRepository(accessToken)

		binding.apply {
			buttonPlus.setOnClickListener {
				showCreateDialog(true)
			}

			buttonMinus.setOnClickListener {
				showCreateDialog(false)
			}

			llAllOperations.setOnClickListener {
				val intent = Intent(activity, AllOperationsActivity::class.java)

				startActivity(intent)
			}

			lvOperationsMonth.setOnItemClickListener { _, _, position, _ ->
				val dialogRemove = DialogRemoveItem( "Удалить операцию?") {
					val requestDelete = CoroutineScope(Dispatchers.Main).async {
						apiOperationRepo.sendDeleteOperationRequest(currentItems[position].id).await()
					}

					requestDelete.invokeOnCompletion {
						val responseDelete = runBlocking { requestDelete.await() }

						if (responseDelete.isSuccessful){
							updateData()

							Toasts.successfully(this@MainFragment.requireContext())
						}
						else
							Toasts.failure(this@MainFragment.requireContext())
					}
				}
				val manager = parentFragmentManager
				dialogRemove.show(manager,"removeDialog")
			}
		}

		// Настройка диаграммы
		configPieChart(binding.pieChart)

		// Запрос валют
		fillCurrencyFromApi()

		fetchCurrentPeriod { periodId ->
			currentPeriodId = periodId

			// Загрузка данных из бд
			updateData()
		}

		return binding.root
	}

	fun showCreateDialog(isPlus: Boolean){
		val dialog = Dialog(this.requireContext())
		dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setCancelable(false)
		dialog.setContentView(R.layout.dialog_create_operation)

		val ivExit = dialog.findViewById<ImageView>(R.id.iv_dialog_create_operation_exit)
		val tvHeader = dialog.findViewById<TextView>(R.id.tv_dialog_create_operation_header)
		val mcvIcon = dialog.findViewById<MaterialCardView>(R.id.mcv_dialog_create_operation_icon)
		val ivIcon = dialog.findViewById<ImageView>(R.id.iv_dialog_create_operation)
		val tvPreview = dialog.findViewById<TextView>(R.id.tv_dialog_create_operation_preview)
		val etTitle = dialog.findViewById<EditText>(R.id.et_dialog_create_operation_title)
		val etAmount = dialog.findViewById<EditText>(R.id.et_dialog_create_operation_amount)
		val rlSave = dialog.findViewById<RelativeLayout>(R.id.rl_dialog_create_operation_save)
		var selectedType: OperationTypeResponse? = null

		ivExit.setOnClickListener {
			dialog.dismiss()
		}

		if (isPlus){
			tvHeader.text = "Доход"
			mcvIcon.strokeColor = getColor(this.requireContext(), R.color.green_main)
		}
		else{
			tvHeader.text = "Расход"
			mcvIcon.strokeColor = getColor(this.requireContext(), R.color.red_crimson)
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

						val iconPath = "https://api.myfinances.tw1.ru/images/${selectedType!!.iconSrc}"
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
			fun EditText.getDouble(): Double = try {
				BigDecimal(text.toString().toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble()
			} catch (e: NumberFormatException) {
				e.printStackTrace()
				0.0
			}

			var title = etTitle.text.toString().trim()
			var amount = etAmount.getDouble()

			if (!isPlus)
				amount *= -1.0

			if (title.isEmpty()){
				Toasts.titleEmpty(this@MainFragment.requireContext()).show()
				return@setOnClickListener
			}

			if (title.length > 30){
				Toasts.titleLong(this@MainFragment.requireContext()).show()
				return@setOnClickListener
			}

			if (amount == 0.0){
				Toasts.amountEmpty(this@MainFragment.requireContext()).show()
				return@setOnClickListener
			}

			if (amount > 999_999.0 || amount < -999_999.0){
				Toasts.amountBig(this@MainFragment.requireContext()).show()
				return@setOnClickListener
			}

			if (selectedType == null){
				Toasts.categoryEmpty(this@MainFragment.requireContext()).show()
				return@setOnClickListener
			}

			val requestCreate = CoroutineScope(Dispatchers.Main).async {
				apiOperationRepo.sendAddOperationRequest(currentPeriodId, title, amount, selectedType!!.id).await()
			}

			requestCreate.invokeOnCompletion {
				val responseCreate = runBlocking { requestCreate.await() }

				if (responseCreate.isSuccessful){
					updateData()
				}

				dialog.dismiss()
			}
		}
	}

	private fun fetchCurrentPeriod(callBackWithPeriodId: (periodId: Int) -> Unit){
		val userId = accessDataRepo.getUserId()

		val requestCurrentPeriod = CoroutineScope(Dispatchers.Main).async {
			apiPeriodRepo.sendCurrentPeriodRequest(userId).await()
		}

		requestCurrentPeriod.invokeOnCompletion {
			val resultCurrentPeriod = runBlocking { requestCurrentPeriod.await() }

			if (resultCurrentPeriod.isSuccessful){
				callBackWithPeriodId(resultCurrentPeriod.success!!.data.id)
			}
			else if (resultCurrentPeriod.error != null && resultCurrentPeriod.error.status == 404){
				val requestNewPeriod = CoroutineScope(Dispatchers.Main).async {
					apiPeriodRepo.sendCreatePeriodRequest(userId).await()
				}

				requestNewPeriod.invokeOnCompletion {
					val resultNewPeriod = runBlocking { requestNewPeriod.await() }

					if (resultNewPeriod.isSuccessful){
						callBackWithPeriodId(resultNewPeriod.success!!.data.id)
					}
				}
			}
		}
	}

	private fun updateData(){
		fillPieChart(binding.pieChart)

		fillMonthOperations()
	}

	private fun fillMonthOperations(){
		binding.lvOperationsMonth.adapter = null

		val requestOperations = CoroutineScope(Dispatchers.Main).async {
			apiOperationRepo.sendOperationsByPeriodRequest(currentPeriodId).await()
		}

		requestOperations.invokeOnCompletion {
			val responseOperations = runBlocking { requestOperations.await() }

			if (responseOperations.isSuccessful){
				val operationsList = responseOperations.success!!.data.toList().reversed()

				if (operationsList.isEmpty()){
					binding.tvEmptyOperations.visibility = View.VISIBLE

					return@invokeOnCompletion
				}

				val requestTypes = CoroutineScope(Dispatchers.Main).async {
					apiTypesRepo.sendAllTypesRequest().await()
				}

				requestTypes.invokeOnCompletion {
					val responseTypes = runBlocking { requestTypes.await() }

					if (responseTypes.isSuccessful) {
						val types = responseTypes.success!!.data.toList()

						val data = operationsList.map { x -> CurrentPeriodItem( x.typeId, x.title, x.amount ) }

						currentItems = operationsList

						val adapter = CurrentPeriodAdapter(this.requireContext(), data, types)

						binding.tvEmptyOperations.visibility = View.INVISIBLE
						binding.lvOperationsMonth.adapter = adapter
						binding.lvOperationsMonth.isClickable = true

						justifyListViewHeightBasedOnChildren(binding.lvOperationsMonth)
					}
					else{
						binding.tvEmptyOperations.visibility = View.VISIBLE

						return@invokeOnCompletion
					}
				}
			}
			else
				binding.tvEmptyOperations.visibility = View.VISIBLE
		}
	}

	private fun fillCurrencyFromApi(){
		val requestUsd = CoroutineScope(Dispatchers.Main).async {
			apiCurrencyRepo.sendCurrencyRequest(Currencies.USD).await()
		}

		val requestEuro = CoroutineScope(Dispatchers.Main).async {
			apiCurrencyRepo.sendCurrencyRequest(Currencies.EUR).await()
		}

		requestUsd.invokeOnCompletion {
			val response = runBlocking { requestUsd.await() }

			if (response.isSuccessful){
				val currency = response.success!!.data.value

				binding.tvCurrencyUsd.text = NumberFormats.FormatToRuble(currency)
			}
			else{
				binding.tvCurrencyUsd.text = getString(R.string.usd_to_ruble)
			}
		}

		requestEuro.invokeOnCompletion {
			val response = runBlocking { requestEuro.await() }

			if (response.isSuccessful){
				val currency = response.success!!.data.value

				binding.tvCurrencyEuro.text = NumberFormats.FormatToRuble(currency)
			}
			else{
				binding.tvCurrencyEuro.text = getString(R.string.euro_to_ruble)
			}
		}
	}

	private fun configPieChart(pieChart: PieChart){
		// Внешние настройки
		pieChart.description.isEnabled = false
		pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

		pieChart.isDrawHoleEnabled = true
		pieChart.setHoleColor(getColor(requireContext(),android.R.color.transparent))
		pieChart.holeRadius = 90f

		pieChart.setDrawCenterText(true)
		pieChart.setCenterTextSize(28f)

		pieChart.isRotationEnabled = false
		pieChart.isHighlightPerTapEnabled = false
		pieChart.legend.isEnabled = false

		pieChart.animateY(2000, Easing.EaseInOutQuad)

		pieChart.highlightValues(null)

		// Создание пустой диаграммы
		clearPieChart()
	}

	private fun clearPieChart(){
		binding.pieChart.clear()

		val entries: ArrayList<PieEntry> = ArrayList()
		val colors: ArrayList<Int> = ArrayList()

		entries.add(PieEntry(1f))

		colors.add(getColor(requireContext(), R.color.gray_dark))

		var dataSet = PieDataSet(entries, "Категории")
		dataSet.sliceSpace = 0f
		dataSet.setDrawValues(false)

		dataSet.colors = colors

		binding.pieChart.data = PieData(dataSet)

		binding.pieChart.centerText = NumberFormats.FormatToRuble(0.0)
		binding.pieChart.setCenterTextColor(getColor(requireContext(), R.color.black))

		binding.pieChart.invalidate()
	}

	private fun fillPieChart(pieChart: PieChart){
		clearPieChart()

		// Месячный бюджет
		val requestProfitOfPeriod = CoroutineScope(Dispatchers.Main).async {
			apiPeriodRepo.sendProfitOfPeriodRequest(currentPeriodId).await()
		}

		requestProfitOfPeriod.invokeOnCompletion {
			val response = runBlocking { requestProfitOfPeriod.await() }
			var expenses = 0.0

			if (response.isSuccessful){
				expenses = response.success!!.data

				pieChart.centerText = NumberFormats.FormatToRuble(expenses)

				if (expenses > 0)
					pieChart.setCenterTextColor(getColor(requireContext(), R.color.green_main))
				else if (expenses < 0)
					pieChart.setCenterTextColor(getColor(requireContext(), R.color.red_crimson))
			}
		}

		// Диаграмма и свойства
		val entries: ArrayList<PieEntry> = ArrayList()
		val colors: ArrayList<Int> = ArrayList()

		val requestSummedGroups = CoroutineScope(Dispatchers.Main).async {
			apiOperationRepo.sendGroupByTypeAndSumRequest(currentPeriodId).await()
		}

		requestSummedGroups.invokeOnCompletion {
			val result = runBlocking { requestSummedGroups.await() }

			if (result.isSuccessful && result.success!!.data.isNotEmpty() && result.success.data[0] != 0){
				val sums = result.success.data

				sums.forEach{
					entries.add(PieEntry(abs(it.toFloat())))
				}

				colors.addAll(ArrayResources.getPieColor(requireContext()).toList())

				val dataSet = PieDataSet(entries, "Категории")
				dataSet.sliceSpace = 0f
				dataSet.setDrawValues(false)

				dataSet.colors = colors

				pieChart.data = PieData(dataSet)

				pieChart.invalidate()
			}
		}
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

	fun justifyListViewHeightBasedOnChildren(listView: ListView) {
		val adapter = listView.adapter ?: return

		val vg: ViewGroup = listView
		var totalHeight = 0
		for (i in 0 until adapter.count) {
			val listItem = adapter.getView(i, null, vg)
			listItem.measure(0, 0)
			totalHeight += listItem.measuredHeight
		}

		val par = listView.layoutParams
		par.height = totalHeight + (listView.dividerHeight * (adapter.count - 1)) + 50
		listView.layoutParams = par
		listView.requestLayout()
	}
}