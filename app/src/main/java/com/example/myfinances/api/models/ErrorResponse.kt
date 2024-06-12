package com.example.myfinances.api.models

data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val errors: ErrorDetail
)

public val RequestError = ErrorResponse(
    "",
    "",
    0,
    ErrorDetail("","",0))