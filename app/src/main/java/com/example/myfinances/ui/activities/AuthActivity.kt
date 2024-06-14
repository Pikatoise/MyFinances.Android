package com.example.myfinances.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myfinances.R
import com.example.myfinances.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)

        binding.tvAuthRegistration.setOnClickListener {
            val i = Intent(this, RegActivity::class.java)

            startActivity(i)

            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        setContentView(binding.root)
    }
}