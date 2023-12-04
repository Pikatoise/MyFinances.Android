package com.example.myfinances

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myfinances.databinding.FragmentPlanBinding

class PlanFragment : Fragment() {
	private lateinit var binding: FragmentPlanBinding
	private lateinit var db: PlanRepository

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentPlanBinding.inflate(inflater,container,false)

		db = PlanRepository(this.requireContext())

		binding.tvMain.text = db.getFirstPlan().name

		return binding.root
	}
}