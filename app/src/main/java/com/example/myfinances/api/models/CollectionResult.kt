package com.example.myfinances.api.models

data class CollectionResult<T> (
    val success: CollectionResponse<T>?,
    val error: ErrorResponse?,
    val isSuccessful: Boolean = error == null
)