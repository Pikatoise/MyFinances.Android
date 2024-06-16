package com.example.myfinances.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.R
import com.example.myfinances.Toasts
import com.example.myfinances.api.repositories.ApiAuthRepository
import com.example.myfinances.databinding.ActivityAuthBinding
import com.example.myfinances.db.AccessDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var apiAuthRepository: ApiAuthRepository
    private lateinit var accessDataRepo: AccessDataRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        apiAuthRepository = ApiAuthRepository()
        accessDataRepo = AccessDataRepository(getSharedPreferences(AccessDataRepository.preferencesName, MODE_PRIVATE))

        binding.tvAuthRegistration.setOnClickListener {
            val i = Intent(this, RegActivity::class.java)

            startActivity(i)

            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        binding.mcvAuthButtonLogin.setOnClickListener {
            val login = binding.etAuthLogin.text.trim().toString()
            val password = binding.etAuthPassword.text.trim().toString()

            if (login.isBlank()){
                Toasts.loginEmpty(this).show()

                return@setOnClickListener
            }

            if (password.isBlank()){
                Toasts.passwordEmpty(this).show()

                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this)
            progressDialog.show()
            progressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            progressDialog.setContentView(R.layout.progress_dialog)

            val request = CoroutineScope(Dispatchers.Main).async {
                apiAuthRepository.sendAuthRequest(login, password).await()
            }

            request.invokeOnCompletion {
                val result = runBlocking { request.await() }

                if (result.isSuccessful){
                    val accessData = result.success!!.data

                    accessDataRepo.updateAccessToken(accessData.accessToken)
                    accessDataRepo.updateRefreshToken(accessData.refreshToken)
                    accessDataRepo.updateUserId(accessData.userId)
                    accessDataRepo.updateLastRefresh(LocalDateTime.now())

                    progressDialog.setContentView(R.layout.successful_dialog)

                    Handler(Looper.getMainLooper()).postDelayed({
                        val i = Intent(this,MainActivity::class.java)
                        startActivity(i)

                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)

                        finish()
                    }, 1500)
                }
                else {
                    progressDialog.setContentView(R.layout.failed_dialog)

                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()

                        val errorDesc = result.error!!.errors.description

                        Toast.makeText(this, errorDesc, Toast.LENGTH_SHORT).show()
                    }, 1500)
                }
            }
        }

        setContentView(binding.root)
    }
}