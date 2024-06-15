package com.example.myfinances.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myfinances.R
import com.example.myfinances.Toasts
import com.example.myfinances.api.repositories.ApiAuthRepository
import com.example.myfinances.databinding.ActivityRegBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class RegActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegBinding
    private lateinit var apiAuthRepository: ApiAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegBinding.inflate(layoutInflater)
        apiAuthRepository = ApiAuthRepository()

        binding.ivRegExit.setOnClickListener{
            finish()

            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        binding.mcvRegButtonRegistration.setOnClickListener {
            val login = binding.etRegLogin.text.trim().toString()
            val password = binding.etRegPassword.text.trim().toString()
            val passwordConfirm = binding.etRegPasswordConfirm.text.trim().toString()

            if (login.isBlank()){
                Toasts.loginEmpty(this).show()

                return@setOnClickListener
            }

            if (login.length < 5){
                Toasts.loginShort(this).show()

                return@setOnClickListener
            }

            if (password.isBlank()){
                Toasts.passwordEmpty(this).show()

                return@setOnClickListener
            }

            if (password.length < 6){
                Toasts.passwordShort(this).show()

                return@setOnClickListener
            }

            if (passwordConfirm.isBlank()){
                Toasts.passwordConfirmEmpty(this).show()

                return@setOnClickListener
            }

            if (passwordConfirm != password){
                Toasts.passwordConfirmNotEqueal(this).show()

                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this)
            progressDialog.show()
            progressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            progressDialog.setContentView(R.layout.progress_dialog)

            val request = CoroutineScope(Dispatchers.Main).async {
                apiAuthRepository.sendRegRequest(login, password, passwordConfirm).await()
            }

            request.invokeOnCompletion {
                val result = runBlocking { request.await() }

                if (result.isSuccessful){
                    progressDialog.setContentView(R.layout.successful_dialog)

                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()

                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
                    }, 1000)
                }
                else {
                    progressDialog.setContentView(R.layout.failed_dialog)

                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()

                        val errorDesc = result.error!!.errors.description

                        Toast.makeText(this, errorDesc, Toast.LENGTH_SHORT).show()
                    }, 1000)
                }
            }
        }

        setContentView(binding.root)
    }
}