package com.example.myfinances

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class DetailedListAdapter(context: Context, dataArrayList: ArrayList<DetailedListData?>?) :
	ArrayAdapter<DetailedListData?>(context, R.layout.detailed_list_item, dataArrayList!!) {

	@RequiresApi(Build.VERSION_CODES.O)
	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var currentView = view
		val detailedListData = getItem(position)

		if (currentView == null) {
			currentView = LayoutInflater.from(context).inflate(R.layout.detailed_list_item, parent, false)
		}

		val listImage = currentView!!.findViewById<ImageView>(R.id.listImage)
		val listName = currentView.findViewById<TextView>(R.id.listName)
		val listAmount = currentView.findViewById<TextView>(R.id.listAmount)
		val listYear = currentView.findViewById<TextView>(R.id.listYear)
		val listMonth = currentView.findViewById<TextView>(R.id.listMonth)

		listImage.setImageResource(detailedListData!!.image)
		listName.text = detailedListData.name
		listAmount.text = detailedListData.amount.toString() + " ₽"
		listYear.text = detailedListData.year.toString()

		when (detailedListData.month) {
			1 -> listMonth.text = "Январь"
			2 -> listMonth.text = "Февраль"
			3 -> listMonth.text = "Март"
			4 -> listMonth.text = "Апрель"
			5 -> listMonth.text = "Май"
			6 -> listMonth.text = "Июнь"
			7 -> listMonth.text = "Июль"
			8 -> listMonth.text = "Август"
			9 -> listMonth.text = "Сентябрь"
			10 -> listMonth.text = "Октябрь"
			11 -> listMonth.text = "Ноябрь"
			12 -> listMonth.text = "Декабрь"
		}

		if (detailedListData.amount < 0)
			listAmount.setTextColor(ContextCompat.getColor(context, R.color.red_crimson))
		else
			listAmount.setTextColor(ContextCompat.getColor(context, R.color.green_main))

		return currentView
	}
}