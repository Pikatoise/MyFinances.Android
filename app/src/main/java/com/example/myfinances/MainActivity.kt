package com.example.myfinances

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.myfinances.databinding.ActivityMainBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.round


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)

		// Change nav bar color to white
		theme.applyStyle(R.style.Theme_GreenNavBar, false)

		// Remove header
		window.requestFeature(Window.FEATURE_NO_TITLE)

		setContentView(binding.root)

		binding.apply {
			ivBurger.setOnClickListener {
				drawer.openDrawer(GravityCompat.START)
			}

			navigationView.setCheckedItem(R.id.item_main)
			replaceFragment(MainFragment())

			navigationView.setNavigationItemSelectedListener {
				if (!it.isChecked){
					it.isChecked = true

					when(it.itemId){
						R.id.item_main -> replaceFragment(MainFragment())
						R.id.item_tips -> replaceFragment(TipsFragment())
						R.id.item_about -> replaceFragment(AboutFragment())
					}
				}

				true
			}
		}
	}

	private fun replaceFragment(fragment: Fragment){
		val fragmentManager = supportFragmentManager
		val fragmentTransaction = fragmentManager.beginTransaction()
		fragmentTransaction.replace(R.id.frameLayout,fragment)
		fragmentTransaction.commit()

		binding.drawer.closeDrawers()
	}
}