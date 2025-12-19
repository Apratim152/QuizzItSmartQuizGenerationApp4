package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.gemini.GeminiService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button to navigate to Create Quiz screen
        val createQuizBtn = findViewById<Button>(R.id.btnCreateQuiz)
        createQuizBtn.setOnClickListener {
            startActivity(Intent(this, LoginCredentials::class.java))
        }

        // Button to test Gemini AI quiz generation
        val testGeminiBtn = findViewById<Button>(R.id.btnTestGemini)
        testGeminiBtn.setOnClickListener {
            testGemini()
        }
    }

    private fun testGemini() {
        lifecycleScope.launch {
            try {
                val prompt = "Generate 1 simple MCQ question on Java"
                val response = GeminiService.generateText(prompt)
                Log.d("GEMINI_TEST", response)
                Toast.makeText(
                    this@MainActivity,
                    "AI Response: $response",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("GEMINI_TEST", "Error generating text", e)
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
