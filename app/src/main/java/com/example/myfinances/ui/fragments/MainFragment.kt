package com.example.myfinances.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat.getColor
import com.example.myfinances.ArrayResources
import com.example.myfinances.Currencies
import com.example.myfinances.ui.activities.AllOperationsActivity
import com.example.myfinances.ui.dialogs.DialogRemoveItem
import com.example.myfinances.lists.CurrentPeriodAdapter
import com.example.myfinances.lists.CurrentPeriodItem
import com.example.myfinances.NumberFormats
import com.example.myfinances.ui.activities.OperationActivity
import com.example.myfinances.db.OperationRepository
import com.example.myfinances.R
import com.example.myfinances.Toasts
import com.example.myfinances.api.models.operation.OperationResponse
import com.example.myfinances.api.repositories.ApiAuthRepository
import com.example.myfinances.api.repositories.ApiCurrencyRepository
import com.example.myfinances.api.repositories.ApiOperationRepository
import com.example.myfinances.api.repositories.ApiOperationTypeRepository
import com.example.myfinances.api.repositories.ApiPeriodRepository
import com.example.myfinances.api.repositories.ApiTokenRepository
import com.example.myfinances.databinding.FragmentMainBinding
import com.example.myfinances.db.AccessDataRepository
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.math.abs

class MainFragment : Fragment() {
	private lateinit var binding: FragmentMainBinding

	private lateinit var db: OperationRepository
	private lateinit var apiAuthRepo: ApiAuthRepository
	private lateinit var apiTokenRepo: ApiTokenRepository
	private lateinit var accessDataRepo: AccessDataRepository
	private lateinit var apiCurrencyRepo: ApiCurrencyRepository
	private lateinit var apiPeriodRepo: ApiPeriodRepository
	private lateinit var apiOperationRepo: ApiOperationRepository
	private lateinit var apiTypesRepo: ApiOperationTypeRepository

	private var currentPeriodId: Int = -1
	private lateinit var currentItems: List<OperationResponse>

	@RequiresApi(Build.VERSION_CODES.O)
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
			tvCurrencyUsd.setOnClickListener {

			}

			buttonPlus.setOnClickListener {
				val intent = Intent(activity, OperationActivity::class.java)

				intent.putExtra("operation",1)

				intent.putExtra("images", ArrayResources.icons)

				startActivityForResult(intent,0)
			}

			buttonMinus.setOnClickListener {
				val intent = Intent(activity, OperationActivity::class.java)

				intent.putExtra("operation",-1)

				intent.putExtra("images", ArrayResources.icons)

				startActivityForResult(intent,0)
			}

			llAllOperations.setOnClickListener {
				val intent = Intent(activity, AllOperationsActivity::class.java)

				intent.putExtra("periods", db.getAllPeriods())
				intent.putExtra("operations", db.getAllOperations())

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

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == 0) {
			if (resultCode == AppCompatActivity.RESULT_OK) {
				val title = data?.getStringExtra("title")
				val amount = data?.getDoubleExtra("amount", 1.0)
				val image = data?.getIntExtra("image", 0)

				db.addOperation(image,title,amount)

				updateData()
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

	@RequiresApi(Build.VERSION_CODES.O)
	private fun updateData(){
		fillPieChart(binding.pieChart)

		fillMonthOperations()
	}

	@RequiresApi(Build.VERSION_CODES.O)
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
		pieChart.setCenterTextColor(getColor(requireContext(), R.color.black))

		pieChart.isRotationEnabled = false
		pieChart.isHighlightPerTapEnabled = false
		pieChart.legend.isEnabled = false

		pieChart.animateY(2000, Easing.EaseInOutQuad)

		pieChart.highlightValues(null)

		// Создание пустой диаграммы
		val entries: ArrayList<PieEntry> = ArrayList()
		val colors: ArrayList<Int> = ArrayList()

		entries.add(PieEntry(1f))

		colors.add(getColor(requireContext(), R.color.gray_dark))

		var dataSet = PieDataSet(entries, "Категории")
		dataSet.sliceSpace = 0f
		dataSet.setDrawValues(false)

		dataSet.colors = colors

		pieChart.data = PieData(dataSet)

		pieChart.centerText = NumberFormats.FormatToRuble(0.0)

		pieChart.invalidate()
	}

	private fun fillPieChart(pieChart: PieChart){
		// Месячный бюджет
		val requestProfitOfPeriod = CoroutineScope(Dispatchers.Main).async {
			apiPeriodRepo.sendProfitOfPeriodRequest(currentPeriodId).await()
		}

		requestProfitOfPeriod.invokeOnCompletion {
			val response = runBlocking { requestProfitOfPeriod.await() }
			var expenses = 0.0

			if (response.isSuccessful)
				expenses = response.success!!.data

			pieChart.centerText = NumberFormats.FormatToRuble(expenses)

			if (expenses > 0)
				pieChart.setCenterTextColor(getColor(requireContext(), R.color.green_main))
			else if (expenses < 0)
				pieChart.setCenterTextColor(getColor(requireContext(), R.color.red_crimson))
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
}