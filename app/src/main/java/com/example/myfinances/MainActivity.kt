package com.example.myfinances

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.databinding.ActivityMainBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.time.Month
import java.time.Year
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.round

@Serializable
data class Rates(val USD: Double, val RUB: Double)
@Serializable
data class Data(val success: Boolean,val timestamp: Int, val base: String,val date: String,val rates: Rates)


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var db: DbRepository
	private lateinit var operationsList: ArrayList<Operation>
	private lateinit var listAdapter: ListAdapter
	private lateinit var listData: ListData
	var dataArrayList = ArrayList<ListData?>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)

		// Change nav bar color to white
		theme.applyStyle(R.style.Theme_GreenNavBar, false)

		// Remove header
		window.requestFeature(Window.FEATURE_NO_TITLE)

		setContentView(binding.root)

		// Инициализация БД
		db = DbRepository(this)

		// Настройка диаграммы
		configPieChart(binding.pieChart)

		// Заполнение диаграммы и месячного бюджета
		fillPieChart(binding.pieChart)

		// Заполнение списка операция текущего месяца
		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val currentMonth = Calendar.getInstance().get(Calendar.MONTH+1)
		fillMonthOperations(currentYear,currentMonth)

		// Отключить на время разработки, дабы не тратить запросы
		//currencyApiRequest()
	}

	fun buttonPlusOperation_Click(view: View){

	}

	fun buttonMinusOperation_Click(view: View){

	}

	private fun fillMonthOperations(year: Int, month: Int){
		operationsList = db.getPeriodOperations(db.getCurrentPeriod().id)

		if (operationsList.isEmpty())
			return

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

		for (operation in operationsList) {
			listData = ListData( imageList[operation.type], operation.title, operation.amount )
			dataArrayList.add(listData)
		}

		listAdapter = ListAdapter(this@MainActivity, dataArrayList)
		binding.lvOperationsMonth.adapter = listAdapter
		binding.lvOperationsMonth.isClickable = true

		binding.lvOperationsMonth.setOnItemClickListener { parent, view, position, id ->
			Toast.makeText(this,operationsList[position].amount.toString(),Toast.LENGTH_SHORT).show()
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
						val body = response?.body?.string()

						runOnUiThread {
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
		pieChart.setHoleColor(getColor(R.color.white))
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
		val expenses = db.getMonthlyExpensesInRub()

		if (expenses >= 0){
			pieChart.setCenterTextColor(getColor(R.color.green_main))
		}
		else{
			pieChart.setCenterTextColor(getColor(R.color.red_light))
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

					typesExpenses[it.type] = oldValue + it.amount
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
		colors.add(getColor(R.color.blue_light))
		colors.add(getColor(R.color.chocolate))
		colors.add(getColor(R.color.gold))
		colors.add(getColor(R.color.green_light))
		colors.add(getColor(R.color.red_crimson))
		colors.add(getColor(R.color.blue_medium))
		colors.add(getColor(R.color.green_dark))
		colors.add(getColor(R.color.gold_dark))
		colors.add(getColor(R.color.violet_medium))
		colors.add(getColor(R.color.blue_dark))
		colors.add(getColor(R.color.violet_dark))
		colors.add(getColor(R.color.orange_dark))

		dataSet.colors = colors

		val data = PieData(dataSet)

		pieChart.data = data

		pieChart.invalidate()
	}
}