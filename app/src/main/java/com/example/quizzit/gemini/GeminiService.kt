package com.example.quizzit.gemini

import com.example.quizzit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object GeminiService {

    // Updated to use a valid model supporting generateContent
    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    private val client = OkHttpClient()

    suspend fun generateText(prompt: String): String = withContext(Dispatchers.IO) {

        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty()) throw Exception("API key missing")

        // 🔹 Add "role" field — required by many models
        val contentObj = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        }

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().put(contentObj))
        }

        val body = requestJson
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL?key=$key")
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
            val candidates = json.getJSONArray("candidates")
            val first = candidates.getJSONObject(0)
            val content = first.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        }
    }
}
