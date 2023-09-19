package com.example.myfinances

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		theme.applyStyle(R.style.Theme_GreenNavBar,false)

		window.requestFeature(Window.FEATURE_NO_TITLE)

		setContentView(R.layout.activity_main)
	}
}