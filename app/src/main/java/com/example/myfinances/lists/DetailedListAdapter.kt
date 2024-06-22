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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.NumberFormats
import com.example.myfinances.R

class DetailedListAdapter(
	context: Context,
	dataArrayList: ArrayList<DetailedListData>) :
	ArrayAdapter<DetailedListData>(context, R.layout.detailed_list_group_item, dataArrayList) {

	@RequiresApi(Build.VERSION_CODES.O)
	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var currentView = view
		val detailedListData = getItem(position)!!

		if (currentView == null) {
			currentView = LayoutInflater.from(context).inflate(R.layout.detailed_list_group_item, parent, false)
		}

		val itemAmount = currentView!!.findViewById<TextView>(R.id.tv_detailed_list_amount)
		val itemDate = currentView.findViewById<TextView>(R.id.tv_detailed_list_date)
		val itemList = currentView.findViewById<ListView>(R.id.lv_detailed_list)
		val clMain = currentView.findViewById<ConstraintLayout>(R.id.cl_main)

		var amount = 0.0

		detailedListData.operations.forEach {
			amount += it.amount
		}

		clMain.layoutParams.height = 100 + detailedListData.operations.count() * 205
		itemDate.text = "${detailedListData.period.year}-${detailedListData.period.month}"
		itemAmount.text = NumberFormats.FormatToRuble(amount)
		itemList.adapter = DetailedListItemAdapter(context ,detailedListData.operations)

		return currentView
	}
}