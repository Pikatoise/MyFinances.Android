package com.example.myfinances.lists

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
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.NumberFormats
import com.example.myfinances.R

class DetailedListAdapter(
	context: Context,
	dataArrayList: ArrayList<DetailedListData>) :
	ArrayAdapter<DetailedListData>(context, R.layout.detailed_list_item, dataArrayList) {

	@RequiresApi(Build.VERSION_CODES.O)
	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var currentView = view
		val detailedListData = getItem(position)!!

		if (currentView == null) {
			currentView = LayoutInflater.from(context).inflate(R.layout.detailed_list_item, parent, false)
		}

		val itemImage = currentView!!.findViewById<ImageView>(R.id.iv_detailed_item_icon)
		val itemTitle = currentView.findViewById<TextView>(R.id.tv_detailed_item_title)
		val itemAmount = currentView.findViewById<TextView>(R.id.tv_detailed_item_amount)
		val itemYear = currentView.findViewById<TextView>(R.id.tv_detailed_item_year)
		val itemMonth = currentView.findViewById<TextView>(R.id.tv_detailed_item_month)

		val iconPath = "https://api.myfinances.tw1.ru/images/${detailedListData.imageSrc}"
		itemImage.loadSvg(iconPath)
		itemTitle.text = detailedListData.name
		itemAmount.text = NumberFormats.FormatToRuble(detailedListData.amount)
		itemYear.text = detailedListData.year.toString()

		when (detailedListData.month) {
			1 -> itemMonth.text = "Январь"
			2 -> itemMonth.text = "Февраль"
			3 -> itemMonth.text = "Март"
			4 -> itemMonth.text = "Апрель"
			5 -> itemMonth.text = "Май"
			6 -> itemMonth.text = "Июнь"
			7 -> itemMonth.text = "Июль"
			8 -> itemMonth.text = "Август"
			9 -> itemMonth.text = "Сентябрь"
			10 -> itemMonth.text = "Октябрь"
			11 -> itemMonth.text = "Ноябрь"
			12 -> itemMonth.text = "Декабрь"
		}

		if (detailedListData.amount < 0)
			itemAmount.setTextColor(ContextCompat.getColor(context, R.color.red_crimson))
		else
			itemAmount.setTextColor(ContextCompat.getColor(context, R.color.green_main))

		return currentView
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
}