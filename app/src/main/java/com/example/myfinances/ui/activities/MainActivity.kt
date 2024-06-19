package com.example.myfinances.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.myfinances.R
import com.example.myfinances.ui.fragments.AboutFragment
import com.example.myfinances.ui.fragments.MainFragment
import com.example.myfinances.ui.fragments.PlanFragment
import com.example.myfinances.ui.fragments.TipsFragment
import com.example.myfinances.databinding.ActivityMainBinding
import com.example.myfinances.db.AccessDataRepository


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var accessDataRepo: AccessDataRepository

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)

		val sharedPreferencesContext = this.getSharedPreferences(AccessDataRepository.preferencesName, MODE_PRIVATE)
		accessDataRepo = AccessDataRepository(sharedPreferencesContext)

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
						R.id.item_plan -> replaceFragment(PlanFragment())
						R.id.item_tips -> replaceFragment(TipsFragment())
						R.id.item_about -> replaceFragment(AboutFragment())
						R.id.item_exit -> {
							accessDataRepo.updateAccessToken("")
							accessDataRepo.updateUserId(-1)
							accessDataRepo.updateRefreshToken("")
							accessDataRepo.updateLastRefresh("")

							var i = Intent(this@MainActivity, AuthActivity::class.java)

							startActivity(i)

							overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)

							finish()
						}
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