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
import kotlin.math.round

@Serializable
data class Rates(val USD: Double, val RUB: Double)
@Serializable
data class Data(val success: Boolean,val timestamp: Int, val base: String,val date: String,val rates: Rates)


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private var usdCurrency: String = "-1"
	private lateinit var db: DbRepository

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)

		// Change nav bar color to white
		theme.applyStyle(R.style.Theme_GreenNavBar, false)

		// Remove header
		window.requestFeature(Window.FEATURE_NO_TITLE)

		setContentView(binding.root)

		db = DbRepository(this)

		fillPieChart(binding.pieChart)

		fillMonthOperations(2023,9)

		binding.pieChart.centerText = fillMonthlyExpenses()

		// Отключить на время разработки, дабы не тратить запросы
		//currencyApiRequest()
	}

	fun Click(view: View){
		var period = db.GetCurrentPeriod()

		Toast.makeText(this,period,Toast.LENGTH_SHORT).show()
	}

	private fun fillMonthlyExpenses(): String{
		return "1 000 000,0 ₽"
	}

	private fun fillMonthOperations(year: Int, month: Int){
		val list = listOf("1", "4", "3", "1", "4", "3", "1", "4", "3", "1", "4", "3")

		binding.lvOperationsMonth.adapter =
			ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
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

	private fun fillPieChart(pieChart: PieChart){
		pieChart.description.isEnabled = false
		pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

		pieChart.isDrawHoleEnabled = true
		pieChart.setHoleColor(getColor(R.color.white))
		pieChart.holeRadius = 90f

		pieChart.setDrawCenterText(true)
		pieChart.centerText = "999 999,99 ₽"
		pieChart.setCenterTextSize(24f)
		pieChart.setCenterTextColor(getColor(R.color.green_main))
		//pieChart.setCenterTextColor(getColor(R.color.red_light))

		pieChart.isRotationEnabled = false
		pieChart.isHighlightPerTapEnabled = false
		pieChart.legend.isEnabled = false

		pieChart.animateY(1000, Easing.EaseInOutQuad)

		val entries: ArrayList<PieEntry> = ArrayList()

		for (i in 60 downTo 5 step 5){
			entries.add(PieEntry(i * 1f))
		}

		val dataSet = PieDataSet(entries, "Траты")

		dataSet.sliceSpace = 0f

		dataSet.setDrawValues(false)

		val colors: ArrayList<Int> = ArrayList()
		colors.add(getColor(R.color.blue_dark))
		colors.add(getColor(R.color.blue_medium))
		colors.add(getColor(R.color.blue_light))
		colors.add(getColor(R.color.chocolate))
		colors.add(getColor(R.color.gold))
		colors.add(getColor(R.color.gold_dark))
		colors.add(getColor(R.color.green_dark))
		colors.add(getColor(R.color.green_light))
		colors.add(getColor(R.color.orange_dark))
		colors.add(getColor(R.color.red_crimson))
		colors.add(getColor(R.color.violet_dark))
		colors.add(getColor(R.color.violet_medium))

		dataSet.colors = colors

		val data = PieData(dataSet)

		pieChart.data = data

		pieChart.highlightValues(null)

		pieChart.invalidate()
	}
}