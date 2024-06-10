package com.example.myfinances.ui.fragments

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myfinances.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {
	private lateinit var binding: FragmentAboutBinding
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentAboutBinding.inflate(inflater,container,false)

		binding.apply {
			tvDeveloperContactsMailContent.setOnClickListener {
				setClipboard(requireContext(),tvDeveloperContactsMailContent.text.toString())

				Toast.makeText(requireContext(),"Email скопирован!", Toast.LENGTH_SHORT).show()
			}

			rlDeveloperContactsTelegram.setOnClickListener {
				val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/pikatoise"))
				startActivity(browserIntent)
			}

			tvDeveloperContactsVk.setOnClickListener {
				val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/myfinancesapp"))
				startActivity(browserIntent)
			}
		}

		return binding.root
	}

	private fun setClipboard(context: Context, text: String) {
		val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
		val clip = ClipData.newPlainText("Copied Text", text)
		clipboard.setPrimaryClip(clip)
	}
}