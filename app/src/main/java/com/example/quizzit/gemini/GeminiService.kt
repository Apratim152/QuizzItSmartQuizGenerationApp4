package com.example.quizzit.gemini

import com.example.quizzit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GeminiService {

    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"



    private val client = OkHttpClient()

    suspend fun generateText(prompt: String): String = withContext(Dispatchers.IO) {

        // 🔹 Build request JSON
        val requestJson = JSONObject().apply {
            put(
                "contents", listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )
        }

        val body = requestJson
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("ERROR: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty response")

            val json = JSONObject(responseBody)

            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }
    }
}
