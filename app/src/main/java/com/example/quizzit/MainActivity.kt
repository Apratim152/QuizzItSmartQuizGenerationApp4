package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.api.QuizService
import com.example.quizzit.api.QuizHelper
import com.example.quizzit.data.entity.QuizEntity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var createQuizBtn: Button
    private lateinit var testGeminiBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createQuizBtn = findViewById(R.id.btnCreateQuiz)
        testGeminiBtn = findViewById(R.id.btnTestGemini)

        // Navigate to CreateQuizActivity
        createQuizBtn.setOnClickListener {
            val intent = Intent(this, CreateQuizActivity::class.java)
            startActivity(intent)
        }

        // Test Gemini AI
        testGeminiBtn.setOnClickListener {
            testGemini()
        }
    }

    private fun testGemini() {
        lifecycleScope.launch {
            try {
                val prompt = "Generate 1 simple MCQ question on Java"

                // Create request using QuizHelper
                val request = QuizHelper.createQuizRequest(prompt, 1)

                // Call Gemini API
                val response = QuizService.quizApi.generateQuiz(request)

                if (response.isSuccessful) {
                    val parsed = QuizHelper.parseQuizResponse(response.body()!!)
                    val questionText = parsed?.cards?.firstOrNull()?.question ?: "No question returned"
                    Toast.makeText(this@MainActivity, questionText, Toast.LENGTH_LONG).show()
                    Log.d("GEMINI_TEST", questionText)
                } else {
                    Toast.makeText(this@MainActivity, "API Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    Log.e("GEMINI_TEST", "API Error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("GEMINI_TEST", "Error generating question", e)
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
