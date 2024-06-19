package com.example.myfinances.api.models.operation

data class OperationResponse(
    val id: Int,
    val title: String,
    val amount: Double,
    val typeId: Int,
    val createdAt: String
)
