package com.example.myfinances.api.models

data class BaseResult<T> (
    val success: SuccessResponse<T>?,
    val error: ErrorResponse?,
    val isSuccessful: Boolean = error == null
)