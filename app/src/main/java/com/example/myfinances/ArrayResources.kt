package com.example.myfinances

import android.content.Context
import androidx.core.content.ContextCompat.getColor

object ArrayResources {
    public val icons = intArrayOf(
        R.drawable.ic_alcohol,
        R.drawable.ic_products,
        R.drawable.ic_taxi,
        R.drawable.ic_bank,
        R.drawable.ic_clothes,
        R.drawable.ic_fun,
        R.drawable.ic_gift,
        R.drawable.ic_house,
        R.drawable.ic_medical,
        R.drawable.ic_salary,
        R.drawable.ic_study,
        R.drawable.ic_cafe
    )

    public fun getPieColor(context: Context) = intArrayOf(
        getColor(context, R.color.orange_dark),
        getColor(context, R.color.violet_dark),
        getColor(context, R.color.blue_light),
        getColor(context, R.color.chocolate),
        getColor(context, R.color.gold),
        getColor(context, R.color.green_light),
        getColor(context, R.color.red_crimson),
        getColor(context, R.color.blue_medium),
        getColor(context, R.color.green_dark),
        getColor(context, R.color.gold_dark),
        getColor(context, R.color.violet_medium),
        getColor(context, R.color.blue_dark),
    )
}