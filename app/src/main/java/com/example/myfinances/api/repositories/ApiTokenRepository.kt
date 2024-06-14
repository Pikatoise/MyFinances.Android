package com.example.myfinances.api.repositories

import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.BaseResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.SuccessResponse
import com.example.myfinances.api.models.auth.LoginResponse
import com.example.myfinances.api.models.token.TokenResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiTokenRepository {
    val client = ApiClient.instance as OkHttpClient
    val url = "https://api.myfinances.tw1.ru/api/Token/"

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
}