package com.example.quizzit.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object QuizHelper {

    fun createQuizRequest(topic: String, cardCount: Int = 5): QuizRequest {
        val prompt = """
            Generate $cardCount multiple choice quiz questions about: $topic
            
            Return ONLY a valid JSON array in this exact format:
            [
              {
                "question": "Question text here?",
                "choices": ["Option A", "Option B", "Option C", "Option D"],
                "correct_index": 0
              }
            ]
            
            Do not include any explanations, markdown formatting, or additional text.
            Just return the raw JSON array.
        """.trimIndent()

        return QuizRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = prompt))
                )
            )
        )
    }

    fun parseQuizResponse(response: QuizResponse): ParsedQuizResponse? {
        return try {
            // Extract the text from Gemini's response
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return null

            // Clean the response (remove extra whitespace)
            val cleanedText = text.trim()

            // Parse the JSON array into QuizCard objects
            val gson = Gson()
            val type = object : TypeToken<List<QuizCard>>() {}.type
            val cards: List<QuizCard> = gson.fromJson(cleanedText, type)

            ParsedQuizResponse(cards = cards)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
