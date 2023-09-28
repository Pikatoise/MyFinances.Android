package com.example.myfinances

data class Operation(var id: Int, var type: Int,var title: String,var amount: Double,var periodId: Int)

data class Period(var id: Int, var year: Int, var month: Int, var isCurrent: Boolean)