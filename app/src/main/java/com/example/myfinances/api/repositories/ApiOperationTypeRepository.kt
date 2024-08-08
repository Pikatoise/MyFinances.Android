package com.example.myfinances.api.repositories

import com.example.myfinances.api.ApiClient
import com.example.myfinances.api.models.CollectionResponse
import com.example.myfinances.api.models.CollectionResult
import com.example.myfinances.api.models.ErrorResponse
import com.example.myfinances.api.models.RequestError
import com.example.myfinances.api.models.operation.OperationResponse
import com.example.myfinances.api.models.operationType.OperationTypeResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ApiOperationTypeRepository(private val accessToken: String) {
    private val client = ApiClient.instance as OkHttpClient
    private val url = ApiClient.SERVER_URL + "OperationType/"

    fun sendAllTypesRequest(): Deferred<CollectionResult<Array<OperationTypeResponse>>> = CoroutineScope(Dispatchers.IO).async {
        val endpoint = ""

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
                val type = Types.newParameterizedType(CollectionResponse::class.java, Array<OperationTypeResponse>::class.java)
                val adapter = moshi.adapter<CollectionResponse<Array<OperationTypeResponse>>>(type)

                val collectionResponse = responseBody?.let { adapter.fromJson(it) }

                return@async CollectionResult<Array<OperationTypeResponse>>(collectionResponse, null)
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