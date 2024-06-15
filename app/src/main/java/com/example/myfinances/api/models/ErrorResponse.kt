package com.example.myfinances.api.models

data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val errors: ErrorDetail
)

public val RequestError = ErrorResponse(
    "",
    "Bad Request",
    500,
    ErrorDetail(
        "Request.Failed",
        "Неудачный запрос",
        0
    )
)