package com.example.myfinances.api

import okhttp3.OkHttpClient

object ApiClient {
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