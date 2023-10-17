package com.example.myfinances

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.databinding.ActivityMainBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
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
	private var dataArrayList = ArrayList<ListData?>()
	private var debugCounter = 0
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

		binding = ActivityMainBinding.inflate(layoutInflater)

		// Change nav bar color to white
		theme.applyStyle(R.style.Theme_GreenNavBar, false)

		// Remove header
		window.requestFeature(Window.FEATURE_NO_TITLE)

		setContentView(binding.root)

		binding.buttonPlus.setOnClickListener {
			val intent = Intent(this,OperationActivity::class.java)

			intent.putExtra("operation",1)

			intent.putExtra("images", imageList)

			startActivityForResult(intent,0)
		}

		binding.buttonMinus.setOnClickListener {
			val intent = Intent(this,OperationActivity::class.java)

			intent.putExtra("operation",-1)

			intent.putExtra("images", imageList)

			startActivityForResult(intent,0)
		}

		binding.tvCurrencyUsd.setOnClickListener{
			debugCounter++

			if (debugCounter == 5){
				enableDebug()
			}
		}

		binding.tvMonthOperatons.setOnClickListener {
			val operationsCount = db.getOperationsCount()
			val periodsCount = db.getPeriodsCount()

			Toast.makeText(this,"Операций $operationsCount\nПериодов $periodsCount",Toast.LENGTH_SHORT).show()
		}

		// Инициализация БД
		db = DbRepository(this)

		// Инициализация текущего периода
		db.updateCurrentPeriod()

		// Настройка диаграммы
		configPieChart(binding.pieChart)

		// Загрузка данных из бд
		updateData()

		// Отключить на время разработки, дабы не тратить запросы
		//currencyApiRequest()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				val title = data?.getStringExtra("title")
				val amount = data?.getDoubleExtra("amount", 1.0)
				val image = data?.getIntExtra("image", 0)

				db.addOperation(image,title,amount)

				updateData()
			}
		}
	}

	private fun updateData(){
		fillPieChart(binding.pieChart)

		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val currentMonth = Calendar.getInstance().get(Calendar.MONTH+1)
		fillMonthOperations(currentYear,currentMonth)
	}

	private fun fillMonthOperations(year: Int, month: Int){
		dataArrayList = ArrayList<ListData?>()
		binding.lvOperationsMonth.adapter = null

		operationsList = db.getPeriodOperations(db.getCurrentPeriod().id)

		if (operationsList.isEmpty()){
			binding.tvEmptyOperations.visibility = View.VISIBLE

			return
		}

		binding.tvEmptyOperations.visibility = View.INVISIBLE

		for (operation in operationsList) {
			listData = ListData( imageList[operation.type], operation.title, operation.amount )
			dataArrayList.add(listData)
		}

		listAdapter = ListAdapter(this@MainActivity, dataArrayList)
		binding.lvOperationsMonth.adapter = listAdapter
		binding.lvOperationsMonth.isClickable = true

		binding.lvOperationsMonth.setOnItemClickListener { _, _, position, _ ->
			val dialogRemove = DialogRemoveOperation {
				db.removeOperation(operationsList[position].id)

				updateData()

				Toast.makeText(this,"Готово",Toast.LENGTH_SHORT).show()
			}
			val manager = supportFragmentManager
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
		pieChart.setHoleColor(getColor(android.R.color.transparent))
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
			pieChart.setCenterTextColor(getColor(R.color.black))
		}
		else if (expenses > 0){
			pieChart.setCenterTextColor(getColor(R.color.green_main))
		}
		else{
			pieChart.setCenterTextColor(getColor(R.color.red_crimson))
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
			colors.add(getColor(R.color.gray_dark))
		}
		else{
			colors.addAll(
				listOf(
					getColor(R.color.orange_dark),
					getColor(R.color.violet_dark),
					getColor(R.color.blue_light),
					getColor(R.color.chocolate),
					getColor(R.color.gold),
					getColor(R.color.green_light),
					getColor(R.color.red_crimson),
					getColor(R.color.blue_medium),
					getColor(R.color.green_dark),
					getColor(R.color.gold_dark),
					getColor(R.color.violet_medium),
					getColor(R.color.blue_dark),
				))
		}

		dataSet.colors = colors

		val data = PieData(dataSet)

		pieChart.data = data

		pieChart.invalidate()
	}

	fun enableDebug() {
		db.DbDebugMode()

		updateData()

		Toast.makeText(this,"Debug mode",Toast.LENGTH_SHORT).show()
	}
}