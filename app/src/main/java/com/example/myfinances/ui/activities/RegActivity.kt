package com.example.myfinances.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myfinances.R
import com.example.myfinances.databinding.ActivityRegBinding

class RegActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegBinding.inflate(layoutInflater)

        binding.mcvRegButtonRegistration.setOnClickListener {

        }

        setContentView(binding.root)
    }
}
//            val progressDialog = ProgressDialog(this)
//            progressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//            progressDialog.setContentView(R.layout.progress_dialog)
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                progressDialog.dismiss()
//            }, 1000)
//            progressDialog.show()
//
//            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
