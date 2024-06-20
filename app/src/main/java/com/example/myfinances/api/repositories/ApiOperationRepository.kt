package com.example.myfinances.api.repositories

import com.example.myfinances.Currencies
import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.BaseResult
import com.example.myfinances.api.models.CollectionResponse
import com.example.myfinances.api.models.CollectionResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.SuccessResponse
import com.example.myfinances.api.models.currency.CurrencyResponse
import com.example.myfinances.api.models.operation.OperationResponse
import com.example.myfinances.api.models.period.PeriodResponse
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

class ApiOperationRepository(private val accessToken: String) {
    val client = ApiClient.instance as OkHttpClient
    val url = "https://api.myfinances.tw1.ru/api/Operation/"

    fun sendGroupByTypeAndSumRequest(periodId: Int): Deferred<CollectionResult<Array<Int>>> = CoroutineScope(
        Dispatchers.IO).async {
        val endpoint = "diagram/GroupByTypeAndSum/$periodId"

        val request = Request.Builder()
            .url(url + endpoint)
            .get()
            .addHeader("Authorization", "Bearer " + accessToken)
            .build()

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(CollectionResponse::class.java, Array<Int>::class.java)
                val adapter = moshi.adapter<CollectionResponse<Array<Int>>>(type)

                val collectionResponse = responseBody?.let { adapter.fromJson(it) }

                return@async CollectionResult<Array<Int>>(collectionResponse, null)
            }
            else{
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val errorAdapter = moshi.adapter(ErrorResponse::class.java)

                val errorResponse = responseBody?.let { errorAdapter.fromJson(it) }

                return@async CollectionResult(null, errorResponse)
            }
        } catch(e: IOException){
            return@async CollectionResult(null, RequestError)
        }
    }

    fun sendOperationsByPeriodRequest(periodId: Int): Deferred<CollectionResult<Array<OperationResponse>>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "$periodId"

        val request = Request.Builder()
            .url(url + endpoint)
            .get()
            .addHeader("Authorization", "Bearer " + accessToken)
            .build()

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(CollectionResponse::class.java, Array<OperationResponse>::class.java)
                val adapter = moshi.adapter<CollectionResponse<Array<OperationResponse>>>(type)

                val collectionResponse = responseBody?.let { adapter.fromJson(it) }

                return@async CollectionResult<Array<OperationResponse>>(collectionResponse, null)
            }
            else{
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val errorAdapter = moshi.adapter(ErrorResponse::class.java)

                val errorResponse = responseBody?.let { errorAdapter.fromJson(it) }

                return@async CollectionResult(null, errorResponse)
            }
        } catch(e: IOException){
            return@async CollectionResult(null, RequestError)
        }
    }

    fun sendDeleteOperationRequest(operationId: Int): Deferred<BaseResult<Boolean>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "remove/$operationId"

        val request = Request.Builder()
            .url(url + endpoint)
            .delete()
            .addHeader("Authorization", "Bearer " + accessToken)
            .build()

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                return@async BaseResult<Boolean>(SuccessResponse(true), null)
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

    fun sendAddOperationRequest(periodId: Int, title: String, amount: Double, typeId: Int): Deferred<BaseResult<Boolean>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "add"

        val json = """
            {
              "periodId": "$periodId",
              "title": "$title",
              "amount": "$amount",
              "typeId": "$typeId"
            }
            """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url + endpoint)
            .post(requestBody)
            .addHeader("Authorization", "Bearer " + accessToken)
            .build()

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                return@async BaseResult<Boolean>(SuccessResponse(true), null)
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