package com.example.myfinances.api.repositories

import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.BaseResult
import com.example.myfinances.api.models.CollectionResponse
import com.example.myfinances.api.models.CollectionResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.SuccessResponse
import com.example.myfinances.api.models.operation.OperationResponse
import com.example.myfinances.api.models.plan.PlanResponse
import com.example.myfinances.db.Plan
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiPlanRepository(private val accessToken: String) {
    val client = ApiClient.instance as OkHttpClient
    val url = "https://api.myfinances.tw1.ru/api/Plan/"

    fun sendUserPlansRequest(userId: Int): Deferred<CollectionResult<Array<PlanResponse>>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "$userId"

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
                val type = Types.newParameterizedType(CollectionResponse::class.java, Array<PlanResponse>::class.java)
                val adapter = moshi.adapter<CollectionResponse<Array<PlanResponse>>>(type)

                val collectionResponse = responseBody?.let { adapter.fromJson(it) }

                return@async CollectionResult<Array<PlanResponse>>(collectionResponse, null)
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

    fun sendDeletePlanRequest(planId: Int): Deferred<BaseResult<Boolean>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "remove/$planId"

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

    fun sendChangeStatusRequest(planId: Int, status: Int): Deferred<BaseResult<Boolean>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "changeStatus/$planId/$status"

        val requestBody = ByteArray(0).toRequestBody()

        val request = Request.Builder()
            .url(url + endpoint)
            .put(requestBody)
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