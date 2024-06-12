package com.example.myfinances.api.models.auth

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int
)
