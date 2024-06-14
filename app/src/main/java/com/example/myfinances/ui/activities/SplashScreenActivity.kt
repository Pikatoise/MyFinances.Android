package com.example.myfinances.ui.activities

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
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class SplashScreenActivity : AppCompatActivity() {
	private lateinit var binding: ActivitySplashScreenBinding
	private lateinit var accessDataRepo: AccessDataRepository
	private lateinit var apiTokenRepo: ApiTokenRepository

	@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		accessDataRepo = AccessDataRepository(this)
		apiTokenRepo = ApiTokenRepository()

		theme.applyStyle(R.style.Theme_GreenNavBar,true)

		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

		binding = ActivitySplashScreenBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.ivLogo.alpha = 0f
		binding.ivLogo.animate().setDuration(1500).alpha(1f ).withEndAction {
			var i = Intent(this, AuthActivity::class.java)

			val refreshTime = accessDataRepo.getLastRefresh()

			if (!refreshTime.isNullOrBlank()){
				val refreshTimeParsed = LocalDateTime.parse(refreshTime)

				val isAccessTokenExpired = apiTokenRepo.checkAccessTokenExpired(refreshTimeParsed)

				if (!isAccessTokenExpired)
					i = Intent(this, MainActivity::class.java)
				else{
					val isRefreshTokenExpired = apiTokenRepo.checkRefreshTokenExpired(refreshTimeParsed)

					if (!isRefreshTokenExpired){
						val refreshToken = accessDataRepo.getRefreshToken() as String
						val accessToken = accessDataRepo.getAccessToken() as String

						val newTokensResponse = runBlocking {
							apiTokenRepo.sendRefreshTokenRequest(accessToken, refreshToken).await()
						}

						if (newTokensResponse.isSuccessful){
							val newTokens = newTokensResponse.success!!.data

							accessDataRepo.updateAccessToken(newTokens.accessToken)
							accessDataRepo.updateRefreshToken(newTokens.refreshToken)
							accessDataRepo.updateLastRefreshToken(LocalDateTime.now())

							i = Intent(this, MainActivity::class.java)
						}
					}
				}
			}

			startActivity(i)

			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)

			finish()
		}
	}
}