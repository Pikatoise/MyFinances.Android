package com.example.myfinances.lists

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.NumberFormats
import com.example.myfinances.R
import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.operationType.OperationTypeResponse

class CurrentPeriodAdapter(
	context: Context,
	data: List<CurrentPeriodItem>,
	val types: List<OperationTypeResponse>) :
	ArrayAdapter<CurrentPeriodItem?>(context, R.layout.list_item, data) {

	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var currentView = view
		val item = getItem(position)!!

		if (currentView == null) {
			currentView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
		}

		val itemImage = currentView!!.findViewById<ImageView>(R.id.listImage)
		val itemTitle = currentView.findViewById<TextView>(R.id.listName)
		val itemAmount = currentView.findViewById<TextView>(R.id.listAmount)

		val iconPath = ApiClient.SERVER_URL_IMAGES + types.find { x -> x.id == item.typeId }!!.iconSrc
		itemImage.loadSvg(iconPath)
		itemTitle.text = item.title
		itemAmount.text = NumberFormats.FormatToRuble(item.amount)

		if (item.amount < 0)
			itemAmount.setTextColor(getColor(context, R.color.red_crimson))
		else
			itemAmount.setTextColor(getColor(context, R.color.green_main))

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