package com.example.quizzit.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QuizApi {

    @POST("play_flashcards") // replace with actual endpoint path
    suspend fun generateQuiz(
        @Body request: QuizRequest
    ): Response<QuizResponse>
}
