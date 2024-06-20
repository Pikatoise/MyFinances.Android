package com.example.myfinances.lists

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.db.Plan
import com.example.myfinances.R

class PlanItemAdapter(
	context: Context,
	dataArrayList: ArrayList<Plan?>?,
	statusCallBack: (status: Boolean, planId: Int)->Unit,
	planClickCallBack: (planId: Int) -> Unit) :

	ArrayAdapter<Plan?>(context, R.layout.plans_list_plan_item, dataArrayList!!) {
		val cbStatus = statusCallBack
		val cbClick = planClickCallBack

	@RequiresApi(Build.VERSION_CODES.O)
	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var currentView = view
		val planItem = getItem(position) as Plan

		if (currentView == null) {
			currentView = LayoutInflater.from(context).inflate(R.layout.plans_list_plan_item, parent, false)
		}

		val ivIcon = currentView!!.findViewById<ImageView>(R.id.iv_plan_icon)
		val tvName = currentView.findViewById<TextView>(R.id.tv_plan_name)
		val cbStatus = currentView.findViewById<CheckBox>(R.id.cb_plan_status)
		val clPlanItem = currentView.findViewById<ConstraintLayout>(R.id.cl_plan_item)

		val iconPath = "https://api.myfinances.tw1.ru/images/${planItem.typePath}"
		ivIcon.loadSvg(iconPath)
		tvName.text = planItem.name
		cbStatus.isChecked = planItem.status
		cbStatus.setOnClickListener {
			cbStatus(cbStatus.isChecked,planItem.id)
		}
		clPlanItem.setOnClickListener {
			cbClick(planItem.id)
		}

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