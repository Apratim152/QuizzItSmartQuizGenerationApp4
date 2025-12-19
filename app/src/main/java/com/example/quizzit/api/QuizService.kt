package com.example.quizzit.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object QuizService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val API_KEY = "AIzaSyDSXhT1qUIrboXnzeku_APDfwdApK_xyD8"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            // Add API key as query parameter (correct way for Gemini API)
            val url = original.url.newBuilder()
                .addQueryParameter("key", API_KEY)
                .build()
            val request = original.newBuilder()
                .url(url)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val quizApi: QuizApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuizApi::class.java)
    }
}