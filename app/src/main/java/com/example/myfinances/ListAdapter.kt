package com.example.myfinances

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor

class ListAdapter(context: Context, dataArrayList: ArrayList<ListData?>?) :
	ArrayAdapter<ListData?>(context, R.layout.list_item_main, dataArrayList!!) {

	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var view = view
		val listData = getItem(position)

		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.list_item_main, parent, false)
		}

		val listImage = view!!.findViewById<ImageView>(R.id.listImage)
		val listName = view.findViewById<TextView>(R.id.listName)
		val listAmount = view.findViewById<TextView>(R.id.listAmount)

		listImage.setImageResource(listData!!.image)
		listName.text = listData.name
		listAmount.text = listData.amount.toString() + " ₽"

		if (listData.amount < 0)
			listAmount.setTextColor(getColor(context,R.color.red_crimson))
		else
			listAmount.setTextColor(getColor(context,R.color.green_main))

		return view
	}
}