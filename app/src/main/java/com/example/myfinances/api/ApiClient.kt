package com.example.myfinances.api

import okhttp3.OkHttpClient

object ApiClient {
    const val SERVER_URL_API = "http://147.45.163.235:5000/api/"
    const val SERVER_URL_IMAGES = "http://147.45.163.235:5000/images/"
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