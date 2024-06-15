package com.example.myfinances.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import com.example.myfinances.ui.activities.AllOperationsActivity
import com.example.myfinances.ui.activities.AuthActivity
import com.example.myfinances.ui.dialogs.DialogRemoveItem
import com.example.myfinances.lists.ListAdapter
import com.example.myfinances.lists.ListData
import com.example.myfinances.NumberFormats
import com.example.myfinances.db.Operation
import com.example.myfinances.ui.activities.OperationActivity
import com.example.myfinances.db.OperationRepository
import com.example.myfinances.R
import com.example.myfinances.Toasts
import com.example.myfinances.api.repositories.ApiAuthRepository
import com.example.myfinances.api.repositories.ApiTokenRepository
import com.example.myfinances.databinding.FragmentMainBinding
import com.example.myfinances.db.AccessData
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
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.math.abs
import kotlin.math.round

@Serializable
data class Rates(val USD: Double, val RUB: Double)
@Serializable
data class Data(val success: Boolean,val timestamp: Int, val base: String,val date: String,val rates: Rates)

class MainFragment : Fragment() {
	private lateinit var binding: FragmentMainBinding
	private lateinit var db: OperationRepository
	private lateinit var operationsList: ArrayList<Operation>
	private var dataArrayList = ArrayList<ListData?>()
	private lateinit var apiAuthRepo: ApiAuthRepository
	private lateinit var apiTokenRepo: ApiTokenRepository
	private lateinit var accessDataRepo: AccessDataRepository
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


	@RequiresApi(Build.VERSION_CODES.O)
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentMainBinding.inflate(inflater,container,false)

		apiAuthRepo = ApiAuthRepository()
		apiTokenRepo = ApiTokenRepository()
		accessDataRepo = AccessDataRepository(this@MainFragment.requireContext())

		binding.apply {
//			tvCurrencyUsd.setOnClickListener {
//				val access = accessDataRepo.getAccessToken()
//				val refresh = accessDataRepo.getRefreshToken()
//				val lastRefresh = accessDataRepo.getLastRefresh()
//
//				Toast.makeText(this@MainFragment.requireContext(), lastRefresh, Toast.LENGTH_SHORT).show()
//			}

			buttonPlus.setOnClickListener {
				val intent = Intent(activity, OperationActivity::class.java)

				intent.putExtra("operation",1)

				intent.putExtra("images", imageList)

				startActivityForResult(intent,0)
			}

			buttonMinus.setOnClickListener {
				val intent = Intent(activity, OperationActivity::class.java)

				intent.putExtra("operation",-1)

				intent.putExtra("images", imageList)

				startActivityForResult(intent,0)
			}

			llAllOperations.setOnClickListener {
				val intent = Intent(activity, AllOperationsActivity::class.java)

				intent.putExtra("periods", db.getAllPeriods())
				intent.putExtra("operations", db.getAllOperations())

				startActivity(intent)
			}
		}

		// Инициализация БД
		db = OperationRepository(this.requireContext())

		// Инициализация текущего периода
		db.updateCurrentPeriod()

		// Настройка диаграммы
		configPieChart(binding.pieChart)

		// Загрузка данных из бд
		updateData()

		//currencyApiRequest()

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

	@RequiresApi(Build.VERSION_CODES.O)
	private fun updateData(){
		fillPieChart(binding.pieChart)

		fillMonthOperations()
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun fillMonthOperations(){
		dataArrayList = ArrayList()
		binding.lvOperationsMonth.adapter = null

		operationsList = db.getPeriodOperations(db.getCurrentPeriod().id)

		if (operationsList.isEmpty()){
			binding.tvEmptyOperations.visibility = View.VISIBLE

			return
		}

		binding.tvEmptyOperations.visibility = View.INVISIBLE

		for (operation in operationsList) {
			val listData = ListData( imageList[operation.type], operation.title, operation.amount )
			dataArrayList.add(listData)
		}

		val listAdapter = ListAdapter(this.requireContext(), dataArrayList)
		binding.lvOperationsMonth.adapter = listAdapter
		binding.lvOperationsMonth.isClickable = true

		binding.lvOperationsMonth.setOnItemClickListener { _, _, position, _ ->
			val dialogRemove = DialogRemoveItem( "Удалить операцию?") {
				db.removeOperation(operationsList[position].id)

				updateData()

				Toast.makeText(activity, "Готово", Toast.LENGTH_SHORT).show()
			}
			val manager = parentFragmentManager
			dialogRemove.show(manager,"removeDialog")
		}
	}

	private fun currencyApiRequest(){
		val url = "http://data.fixer.io/api/latest?access_key=4e07385f3f73922895d21be02e123181&symbols=USD,RUB&format=1"

		val currencyFetch = OkHttpClient()

		val request = Request.Builder().url(url).build()

		currencyFetch.newCall(request).enqueue(object: Callback {
			override fun onFailure(call: Call, e: IOException) {
				Log.d("Error: Currency fetching ",e.message.toString())
			}

			override fun onResponse(call: Call, response: Response) {
				response.use {
					if (!response.isSuccessful){
						Log.e("HTTP Error","Something didn't load")
					}
					else{
						val body = response.body?.string()

						activity?.runOnUiThread {
							updateCurrency(body.toString())
						}
					}
				}
			}
		})
	}

	private fun updateCurrency(jsonData: String) {
		var response = Json.decodeFromString<Data>(jsonData)

		binding.tvCurrencyUsd.text = (round((response.rates.RUB / response.rates.USD) * 100) / 100).toString()
		binding.tvCurrencyEuro.text = (round(response.rates.RUB * 100) / 100).toString()
	}

	private fun configPieChart(pieChart: PieChart){
		pieChart.description.isEnabled = false
		pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

		pieChart.isDrawHoleEnabled = true
		pieChart.setHoleColor(getColor(requireContext(),android.R.color.transparent))
		pieChart.holeRadius = 90f

		pieChart.setDrawCenterText(true)
		pieChart.setCenterTextSize(24f)

		pieChart.isRotationEnabled = false
		pieChart.isHighlightPerTapEnabled = false
		pieChart.legend.isEnabled = false

		pieChart.animateY(1000, Easing.EaseInOutQuad)

		pieChart.highlightValues(null)

		pieChart.invalidate()
	}

	private fun fillPieChart(pieChart: PieChart){
		// Месячный бюджет
		val expenses = db.getMonthlyExpensesInRub(db.getCurrentPeriod())

		if (expenses == 0.0){
			pieChart.setCenterTextColor(getColor(requireContext(), R.color.black))
		}
		else if (expenses > 0){
			pieChart.setCenterTextColor(getColor(requireContext(), R.color.green_main))
		}
		else{
			pieChart.setCenterTextColor(getColor(requireContext(), R.color.red_crimson))
		}

		pieChart.centerText = NumberFormats.FormatToRuble(expenses)

		// Диаграмма и свойства

		val entries: ArrayList<PieEntry> = ArrayList()

		val operations = db.getPeriodOperations(db.getCurrentPeriod().id)

		val typesExpenses: MutableMap<Int, Double> = mutableMapOf()

		if (operations.isEmpty()){
			entries.add(PieEntry(1f))
		}
		else{
			operations.forEach {
				if (typesExpenses.containsKey(it.type)){
					var oldValue = typesExpenses.get(it.type)!!

					typesExpenses[it.type] = abs(oldValue) + abs(it.amount)
				}
				else{
					typesExpenses[it.type] = it.amount
				}
			}

			typesExpenses.forEach{
				entries.add(PieEntry(abs(it.value.toFloat())))
			}
		}


		val dataSet = PieDataSet(entries, "Категории")

		dataSet.sliceSpace = 0f

		dataSet.setDrawValues(false)

		val colors: ArrayList<Int> = ArrayList()

		if (entries.count() == 1 && entries[0].value == 1f){
			colors.add(getColor(requireContext(), R.color.gray_dark))
		}
		else{
			colors.addAll(
				listOf(
					getColor(requireContext(), R.color.orange_dark),
					getColor(requireContext(), R.color.violet_dark),
					getColor(requireContext(), R.color.blue_light),
					getColor(requireContext(), R.color.chocolate),
					getColor(requireContext(), R.color.gold),
					getColor(requireContext(), R.color.green_light),
					getColor(requireContext(), R.color.red_crimson),
					getColor(requireContext(), R.color.blue_medium),
					getColor(requireContext(), R.color.green_dark),
					getColor(requireContext(), R.color.gold_dark),
					getColor(requireContext(), R.color.violet_medium),
					getColor(requireContext(), R.color.blue_dark),
				))
		}

		dataSet.colors = colors

		val data = PieData(dataSet)

		pieChart.data = data

		pieChart.invalidate()
	}
}