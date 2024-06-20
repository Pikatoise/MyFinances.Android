package com.example.myfinances.api.models.plan

data class PlanResponse(
    val id: Int,
    val name: String,
    val amount: Int,
    val finalDate: String,
    val status: Int,
    val typeIconSrc: String
)
