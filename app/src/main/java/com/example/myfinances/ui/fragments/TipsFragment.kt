package com.example.myfinances.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myfinances.databinding.FragmentTipsBinding

class TipsFragment : Fragment() {
	private lateinit var binding: FragmentTipsBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentTipsBinding.inflate(inflater,container,false)

		return binding.root
	}
}