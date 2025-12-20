package com.example.quizzit.api

// Represents a message content for Gemini API
data class Content(
    val role: String,
    val parts: List<Part>
)

// Represents a part of the content (usually text)
data class Part(
    val text: String
)

// Represents a single quiz card returned by Gemini
data class QuizCard(
    val question: String,
    val choices: List<String>,
    val correct_index: Int
)

// Parsed response wrapper
data class ParsedQuizResponse(
    val cards: List<QuizCard>
)

// API response models
data class QuizRequest(
    val contents: List<Content>
)

data class QuizResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)
