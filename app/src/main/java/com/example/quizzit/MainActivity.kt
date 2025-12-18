package com.example.quizzit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.gemini.GeminiService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testGemini()
    }

    private fun testGemini() {
        lifecycleScope.launch {
            try {
                val result = GeminiService.generateText(
                    "Generate 1 simple MCQ question on Java"
                )
                Log.d("GEMINI_TEST", result)
            } catch (e: Exception) {
                Log.e("GEMINI_TEST", "Error", e)
            }
        }
    }
}
