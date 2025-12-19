package com.example.quizzit.api

data class QuizRequest(
    val nextQuizTitle: String,
    val requestedCardCount: Int = 5
)

data class QuizCard(
    val question: String,
    val choices: List<String>,
    val correct_index: Int
)

data class QuizResponse(
    val cards: List<QuizCard>
)
