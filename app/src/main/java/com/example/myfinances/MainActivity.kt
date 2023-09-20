package com.example.myfinances

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate
import com.example.myfinances.databinding.ActivityMainBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)

		// Change nav bar color to white
		theme.applyStyle(R.style.Theme_GreenNavBar,false)

		// Remove header
		window.requestFeature(Window.FEATURE_NO_TITLE)

		setContentView(binding.root)

		fillPieChart(binding.pieChart)
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