package com.example.myfinances.api.repositories

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.BaseResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.SuccessResponse
import com.example.myfinances.api.models.auth.LoginResponse
import com.example.myfinances.api.models.token.TokenResponse
import com.example.myfinances.db.AccessDataRepository
import com.example.myfinances.ui.activities.MainActivity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.exp

class ApiTokenRepository {
    val client = ApiClient.instance as OkHttpClient
    val url = "https://api.myfinances.tw1.ru/api/Token/"
    val refreshTokenLifetimeDays = 7
    val accessTokenLifetimeMinutes = 30

    fun sendRefreshTokenRequest(accessToken: String, refreshToken: String): Deferred<BaseResult<TokenResponse>> = CoroutineScope(Dispatchers.IO).async{
        val endpoint = ""

        val json = """
            {
              "accessToken": "$accessToken",
              "refreshToken": "$refreshToken"
            }
            """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url + endpoint)
            .post(requestBody)
            .build()

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(SuccessResponse::class.java, TokenResponse::class.java)
                val adapter = moshi.adapter<SuccessResponse<TokenResponse>>(type)

                val successResponse = responseBody?.let { adapter.fromJson(it) }

                return@async BaseResult<TokenResponse>(successResponse, null)
            }
            else{
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val errorAdapter = moshi.adapter(ErrorResponse::class.java)

                val errorResponse = responseBody?.let { errorAdapter.fromJson(it) }

                return@async BaseResult(null, errorResponse)
            }
        } catch(e: IOException){
            return@async BaseResult(null, RequestError)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAccessTokenExpired(refreshTime: LocalDateTime): Boolean{
        var isExpired = true
        val currentTime = LocalDateTime.now()
        val expiredTime = refreshTime.plusMinutes(accessTokenLifetimeMinutes.toLong())

        if (currentTime.isBefore(expiredTime))
            isExpired = false

        return isExpired
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkRefreshTokenExpired(refreshTime: LocalDateTime): Boolean{
        var isExpired = true
        val currentTime = LocalDateTime.now()
        val expiredTime = refreshTime.plusDays(refreshTokenLifetimeDays.toLong())

        if (currentTime.isBefore(expiredTime))
            isExpired = false

        return isExpired
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun tryPassSavedTokens(accessDataRepo: AccessDataRepository): Boolean {
        val refreshTime = accessDataRepo.getLastRefresh()
        var canAccess = false

        if (!refreshTime.isNullOrBlank()) {
            val refreshTimeParsed = LocalDateTime.parse(refreshTime)

            val isAccessTokenExpired = checkAccessTokenExpired(refreshTimeParsed)

            if (!isAccessTokenExpired)
                canAccess = true
            else {
                val isRefreshTokenExpired = checkRefreshTokenExpired(refreshTimeParsed)

                if (!isRefreshTokenExpired) {
                    val refreshToken = accessDataRepo.getRefreshToken() as String
                    val accessToken = accessDataRepo.getAccessToken() as String

                    val result = runBlocking {
                        sendRefreshTokenRequest(accessToken, refreshToken).await()
                    }

                    if (result.isSuccessful) {
                        val newTokens = result.success!!.data

                        accessDataRepo.updateAccessToken(newTokens.accessToken)
                        accessDataRepo.updateRefreshToken(newTokens.refreshToken)
                        accessDataRepo.updateLastRefresh(LocalDateTime.now().toString())

                        canAccess = true
                    }
                }
            }
        }

        return canAccess
    }
}