package com.example.myfinances.lists

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.NumberFormats
import com.example.myfinances.R

class DetailedListItemAdapter(
    context: Context,
    dataArrayList: ArrayList<DetailedListItemData>) :
    ArrayAdapter<DetailedListItemData>(context, R.layout.detailed_list_item, dataArrayList) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var currentView = view
        val detailedListItemData = getItem(position)!!

        if (currentView == null) {
            currentView = LayoutInflater.from(context).inflate(R.layout.detailed_list_item, parent, false)
        }

        val itemImage = currentView!!.findViewById<ImageView>(R.id.iv_detailed_item_icon)
        val itemTitle = currentView.findViewById<TextView>(R.id.tv_detailed_item_title)
        val itemAmount = currentView.findViewById<TextView>(R.id.tv_detailed_item_amount)

		val iconPath = "https://api.myfinances.tw1.ru/images/${detailedListItemData.imageSrc}"
		itemImage.loadSvg(iconPath)
		itemTitle.text = detailedListItemData.name
		itemAmount.text = NumberFormats.FormatToRuble(detailedListItemData.amount)

		if (detailedListItemData.amount < 0)
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