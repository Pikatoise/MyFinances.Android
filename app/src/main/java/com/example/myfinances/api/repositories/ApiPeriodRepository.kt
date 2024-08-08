package com.example.myfinances.api.repositories

import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.BaseResult
import com.example.myfinances.api.models.CollectionResponse
import com.example.myfinances.api.models.CollectionResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.SuccessResponse
import com.example.myfinances.api.models.operationType.OperationTypeResponse
import com.example.myfinances.api.models.period.PeriodResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonNull.content
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.Period


class ApiPeriodRepository(private val accessToken: String) {
    private val client = ApiClient.instance as OkHttpClient
    private val url = ApiClient.SERVER_URL + "Period/"

    fun sendProfitOfPeriodRequest(periodId: Int): Deferred<BaseResult<Double>> = CoroutineScope(
        Dispatchers.IO).async {
        val endpoint = "profitOf/$periodId"

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
                val type = Types.newParameterizedType(SuccessResponse::class.java, Double::class.javaObjectType)
                val adapter = moshi.adapter<SuccessResponse<Double>>(type)

                val successResponse = responseBody?.let { adapter.fromJson(it) }

                return@async BaseResult<Double>(successResponse, null)
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

    fun sendCurrentPeriodRequest(userId: Int): Deferred<BaseResult<PeriodResponse>> = CoroutineScope(Dispatchers.IO).async {
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
                val type = Types.newParameterizedType(SuccessResponse::class.java, PeriodResponse::class.java)
                val adapter = moshi.adapter<SuccessResponse<PeriodResponse>>(type)

                val successResponse = responseBody?.let { adapter.fromJson(it) }

                return@async BaseResult<PeriodResponse>(successResponse, null)
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

    fun sendCreatePeriodRequest(userId: Int): Deferred<BaseResult<PeriodResponse>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "add/$userId"

        val requestBody = ByteArray(0).toRequestBody()

        val request = Request.Builder()
            .url(url + endpoint)
            .post(requestBody)
            .addHeader("Authorization", "Bearer " + accessToken)
            .build()

        try{
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(SuccessResponse::class.java,PeriodResponse::class.java)
                val adapter = moshi.adapter<SuccessResponse<PeriodResponse>>(type)

                val successResponse = responseBody?.let { adapter.fromJson(it) }

                return@async BaseResult<PeriodResponse>(successResponse, null)
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

    fun sendAllPeriodsRequest(userId: Int): Deferred<CollectionResult<Array<PeriodResponse>>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = "all/$userId"

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
                val type = Types.newParameterizedType(CollectionResponse::class.java, Array<PeriodResponse>::class.java)
                val adapter = moshi.adapter<CollectionResponse<Array<PeriodResponse>>>(type)

                val collectionResponse = responseBody?.let { adapter.fromJson(it) }

                return@async CollectionResult<Array<PeriodResponse>>(collectionResponse, null)
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
}