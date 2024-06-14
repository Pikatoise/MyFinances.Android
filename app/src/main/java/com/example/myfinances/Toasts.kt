package com.example.myfinances

import android.content.Context
import android.widget.Toast

object Toasts {
    public val requestFailed: (context: Context) -> Toast = {
        context -> Toast.makeText(context,
        "Ошибка запроса",
        Toast.LENGTH_SHORT)
    }

}