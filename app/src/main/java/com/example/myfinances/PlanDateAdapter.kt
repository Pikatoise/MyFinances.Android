package com.example.myfinances

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

class PlanDateAdapter (context: Context, dataArrayList: ArrayList<PlanDateData?>?) :
	ArrayAdapter<PlanDateData?>(context, R.layout.plans_list_date_item, dataArrayList!!) {

	@RequiresApi(Build.VERSION_CODES.O)
	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var currentView = view
		val planDateItem = getItem(position)

		if (currentView == null) {
			currentView = LayoutInflater.from(context).inflate(R.layout.plans_list_date_item, parent, false)
		}

		val tvDate = currentView!!.findViewById<TextView>(R.id.tv_date)
		val tvList = currentView.findViewById<ListView>(R.id.lv_plans)
		val clMain = currentView.findViewById<ConstraintLayout>(R.id.cl_main)

		clMain.layoutParams.height = 100 + planDateItem!!.items!!.count() * 190
		tvDate.text = planDateItem!!.date
		val planItemsAdapter = PlanItemAdapter(context,planDateItem.items)
		tvList.adapter = planItemsAdapter

		return currentView
	}
}