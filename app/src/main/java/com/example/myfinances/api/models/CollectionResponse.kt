package com.example.myfinances.api.models

data class CollectionResponse<T>(
    val count: Int,
    val data: T
)
