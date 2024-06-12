package com.example.myfinances.api.models

data class ErrorDetail(
    val code: String,
    val description: String,
    val type: Int
)