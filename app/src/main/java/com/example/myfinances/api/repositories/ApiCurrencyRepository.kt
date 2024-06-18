package com.example.myfinances.api.repositories

import android.util.Log
import com.example.myfinances.Currencies
import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.BaseResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.SuccessResponse
import com.example.myfinances.api.models.auth.LoginResponse
import com.example.myfinances.api.models.currency.CurrencyResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.Console
import java.io.IOException

class ApiCurrencyRepository constructor(private val accessToken: String) {
    private val client = ApiClient.instance as OkHttpClient
    private val url = "https://api.myfinances.tw1.ru/api/Currency/"

    fun sendCurrencyRequest(currency: Currencies): Deferred<BaseResult<CurrencyResponse>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = currency.toString()

        val request = Request.Builder()
            .url(url + endpoint)
            .get()
            .addHeader("Authorization", "Bearer " + accessToken)
            .build()

        Log.i("*******", accessToken)

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(SuccessResponse::class.java,CurrencyResponse::class.java)
                val adapter = moshi.adapter<SuccessResponse<CurrencyResponse>>(type)

                val successResponse = responseBody?.let { adapter.fromJson(it) }

                return@async BaseResult<CurrencyResponse>(successResponse, null)
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