package com.example.quizzit.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
interface QuizApi {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")  // Changed model name
    suspend fun generateQuiz(
        @Body request: QuizRequest
    ): Response<QuizResponse>
}