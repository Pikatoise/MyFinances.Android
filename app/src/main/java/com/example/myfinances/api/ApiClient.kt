package com.example.myfinances.api

import okhttp3.OkHttpClient

object ApiClient {
    const val SERVER_URL = "http://147.45.163.235:5000/api/"
    private var client: OkHttpClient? = null

    val instance: OkHttpClient?
        get() {
            if (client == null) {
                client =
                    OkHttpClient().newBuilder()
                        .build()
            }
            return client
        }
}