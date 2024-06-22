package com.example.myfinances.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.example.myfinances.R
import com.example.myfinances.api.repositories.ApiTokenRepository
import com.example.myfinances.databinding.ActivitySplashScreenBinding
import com.example.myfinances.db.AccessDataRepository

class SplashScreenActivity : AppCompatActivity() {
	private lateinit var binding: ActivitySplashScreenBinding
	private lateinit var accessDataRepo: AccessDataRepository
	private lateinit var apiTokenRepo: ApiTokenRepository

	@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		accessDataRepo = AccessDataRepository(getSharedPreferences(AccessDataRepository.preferencesName, Context.MODE_PRIVATE))
		apiTokenRepo = ApiTokenRepository()

		theme.applyStyle(R.style.Theme_GreenNavBar,true)

		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

		binding = ActivitySplashScreenBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.ivLogo.alpha = 0f
		binding.ivLogo.animate().setDuration(1500).alpha(1f ).withEndAction {
			var i = Intent(this, AuthActivity::class.java)

			val authResult = apiTokenRepo.tryPassSavedTokens(accessDataRepo)

			if (authResult)
				i = Intent(this, MainActivity::class.java)

			startActivity(i)

			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)

			finish()
		}
	}
}