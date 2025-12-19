package com.example.quizzit.api

// Gemini API Request format
data class QuizRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

// Gemini API Response format
data class QuizResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

data class SafetyRating(
    val category: String,
    val probability: String
)

// Your app's internal quiz models
data class QuizCard(
    val question: String,
    val choices: List<String>,
    val correct_index: Int
)

data class ParsedQuizResponse(
    val cards: List<QuizCard>
)